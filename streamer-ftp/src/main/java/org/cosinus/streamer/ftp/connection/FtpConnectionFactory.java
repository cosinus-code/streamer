/*
 * Copyright 2025 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cosinus.streamer.ftp.connection;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.remote.AbstractConnectionFactory;
import org.cosinus.streamer.ftp.credentials.FtpCredentials;
import org.cosinus.streamer.ftp.credentials.FtpCredentialsModel;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.AbortActionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.formdev.flatlaf.util.StringUtils.isEmpty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.net.ftp.FTPClientConfig.SYST_UNIX;
import static org.apache.commons.net.ftp.FTPReply.isPositiveCompletion;
import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_CREDENTIALS_DIALOG;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class FtpConnectionFactory extends AbstractConnectionFactory<String, FtpConnection, FTPFile> {

    private static final Logger LOG = LogManager.getLogger(FtpConnectionFactory.class);

    private final NetworkConfigurationProvider networkConfigurationProvider;

    private final DialogHandler dialogHandler;

    private final Map<String, FtpCredentials> credentialsCache = new ConcurrentHashMap<>();

    public FtpConnectionFactory(final NetworkConfigurationProvider networkConfigurationProvider,
                                final DialogHandler dialogHandler) {
        this.networkConfigurationProvider = networkConfigurationProvider;
        this.dialogHandler = dialogHandler;
    }

    /**
     * Create a connection to a ftp host.
     * <p>
     * If the host has no username or password configured, the user is asked for them.
     * If the login fails with the entered credentials, the user is asked again,
     * until the login succeeds or the user cancels.
     * If the user cancels, an {@link AbortActionException} is thrown, so the action is aborted without an error.
     *
     * @param name the host name
     * @return the connection, or null if the server refused the connection or the login with the configured credentials
     * @throws Exception if the connection fails
     */
    @Override
    public FtpConnection create(String name) throws Exception {
        return connectAndLogin(name, getFtpConfigurationWithCachedCredentials(name));
    }

    /**
     * Connect and log in to a ftp host, asking the user for the missing credentials.
     * <p>
     * If the login fails with credentials entered by the user, the user is asked again for credentials,
     * and the connection is retried with them.
     *
     * @param name             the host name
     * @param ftpConfiguration the host configuration
     * @return the connection, or null if the server refused the connection or the login with the configured credentials
     * @throws Exception if the connection fails
     */
    private FtpConnection connectAndLogin(final String name,
                                          final FtpConfiguration ftpConfiguration) throws Exception {
        FtpConfiguration configuration = ftpConfiguration.hasCredentials() ?
            askCredentials(name, ftpConfiguration) :
            ftpConfiguration;

        FtpConnection ftpConnection = connect(name, configuration);
        if (ftpConnection == null) {
            return null;
        }
        if (login(ftpConnection, configuration)) {
            if (configuration.passiveMode()) {
                ftpConnection.enterLocalPassiveMode();
            }
            if (!isEmpty(configuration.remoteDir())) {
                ftpConnection.changeWorkingDirectory(configuration.remoteDir());
            }
            return ftpConnection;
        }

        LOG.warn("FTP login to server {} failed for username {}",
            configuration.host(),
            configuration.username());
        destroyConnection(ftpConnection);
        return hasCachedCredentials(name, configuration) ?
            connectAndLogin(name, askCredentials(name, configuration)) :
            null;
    }

    /**
     * Open the connection to the ftp server, without logging in.
     *
     * @param name             the host name
     * @param ftpConfiguration the host configuration
     * @return the connection, or null if the server refused the connection
     * @throws IOException if the connection fails
     */
    protected FtpConnection connect(final String name,
                                    final FtpConfiguration ftpConfiguration) throws IOException {
        FtpConnection ftpConnection = new FtpConnection(name);
        ftpConnection.configure(new FTPClientConfig(SYST_UNIX));
        ofNullable(ftpConfiguration.encoding())
            .ifPresent(ftpConnection::setControlEncoding);
        ftpConnection.setConnectTimeout(ftpConfiguration.connectionTimeout() * 1000);

        ftpConnection.connect(ftpConfiguration.host(), ftpConfiguration.port());
        int replyCode = ftpConnection.getReplyCode();
        if (!isPositiveCompletion(replyCode)) {
            ftpConnection.disconnect();
            LOG.warn("FTP server {} refused connection: {}",
                ftpConfiguration.host(),
                replyCode);
            return null;
        }
        LOG.info("Connected to FTP server {}", ftpConfiguration.host());
        LOG.info(ftpConnection.getReplyString());
        return ftpConnection;
    }

    protected boolean login(final FtpConnection ftpConnection,
                            final FtpConfiguration ftpConfiguration) throws IOException {
        return ftpConnection.login(ftpConfiguration.username(), ftpConfiguration.password());
    }

    private FtpConfiguration addCachedCredentialsToConfiguration(final FtpConfiguration ftpConfiguration) {
        return ofNullable(credentialsCache.get(ftpConfiguration.name()))
            .map(credentials -> ftpConfiguration.withCredentials(credentials.username(), credentials.password()))
            .orElse(ftpConfiguration);
    }

    private boolean hasCachedCredentials(final String name, final FtpConfiguration ftpConfiguration) {
        return ofNullable(credentialsCache.get(name))
            .filter(credentials -> credentials.isUsedBy(ftpConfiguration))
            .isPresent();
    }

    /**
     * Ask the user for the credentials to log in to a ftp host, in a dialog.
     * <p>
     * If other credentials were entered for the same host, before or while waiting for another credentials dialog
     * to be closed, they are used instead, without showing the dialog.
     *
     * @param name                the host name
     * @param deniedConfiguration the host configuration without credentials, or whose credentials were refused
     * @return the host configuration, with the entered credentials
     * @throws AbortActionException if the user cancel
     */
    private FtpConfiguration askCredentials(String name, FtpConfiguration deniedConfiguration) {
        FtpCredentials credentials = getCredentialsForConfiguration(name, deniedConfiguration)
            .or(() -> dialogHandler.lockAndShowDialog(applicationFrame,
                    FTP_CREDENTIALS_DIALOG,
                    () -> getCredentialsForConfiguration(name, deniedConfiguration).isPresent() ?
                        Optional.empty() :
                        Optional.of(new FtpCredentialsModel(deniedConfiguration.description(),
                            deniedConfiguration.username())))
                .map(FtpCredentialsModel::toCredentials)
                .or(() -> getCredentialsForConfiguration(name, deniedConfiguration)))
            .orElseThrow(() -> new AbortActionException("FTP credentials cancelled by user"));
        credentialsCache.put(name, credentials);
        return deniedConfiguration.withCredentials(credentials.username(), credentials.password());
    }

    /**
     * @param name                the host name
     * @param deniedConfiguration the host configuration without credentials, or whose credentials were refused
     * @return the credentials entered for the host, if they are not the refused ones
     */
    private Optional<FtpCredentials> getCredentialsForConfiguration(String name, FtpConfiguration deniedConfiguration) {
        return ofNullable(credentialsCache.get(name))
            .filter(credentials -> !credentials.isUsedBy(deniedConfiguration));
    }

    @Override
    public boolean validateConnection(FtpConnection ftpConnection) {
        return isPositiveCompletion(runValidateCommand(ftpConnection));
    }

    protected int runValidateCommand(FtpConnection ftpConnection) {
        try {
            return ftpConnection.noop();
        } catch (IOException e) {
            LOG.error("FTP connection validation failed", e);
            return 0;
        }
    }

    @Override
    public void destroyConnection(FtpConnection ftpConnection) {
        try {
            if (ftpConnection.isConnected()) {
                ftpConnection.logout();
            }
        } catch (IOException io) {
            LOG.error("FTP logout failed", io);
        } finally {
            try {
                ftpConnection.disconnect();
            } catch (IOException io) {
                LOG.error("FTP disconnect failed", io);
            }
        }
    }

    public FtpConfiguration getFtpConfigurationWithCachedCredentials(String name) {
        return addCachedCredentialsToConfiguration(getFtpConfiguration(name));
    }

    public FtpConfiguration getFtpConfiguration(String name) {
        return networkConfigurationProvider.getHost(name, FtpConfiguration.class)
            .orElseThrow(() -> new StreamerException("FTP connection configuration not found: " + name));
    }
}
