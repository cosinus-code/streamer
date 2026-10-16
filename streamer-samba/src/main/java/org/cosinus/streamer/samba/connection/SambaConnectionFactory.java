/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.streamer.samba.connection;

import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.remote.AbstractConnectionFactory;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.streamer.samba.credentials.SambaCredentials;
import org.cosinus.streamer.samba.credentials.SambaCredentialsModel;
import org.cosinus.streamer.samba.credentials.SambaWorkgroupProvider;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.AbortActionException;
import org.springframework.stereotype.Component;

import java.net.NoRouteToHostException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.hierynomus.mserref.NtStatus.STATUS_ACCESS_DENIED;
import static com.hierynomus.mserref.NtStatus.STATUS_ACCOUNT_DISABLED;
import static com.hierynomus.mserref.NtStatus.STATUS_LOGON_FAILURE;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.indexOfType;
import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_CREDENTIALS_DIALOG;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class SambaConnectionFactory extends AbstractConnectionFactory<String, SambaConnection, SambaFile> {

    private static final Logger LOG = LogManager.getLogger(SambaConnectionFactory.class);

    /**
     * Separator between the host and the share in the connection key.
     * Both are path elements, so they can't contain it.
     */
    private static final String KEY_SEPARATOR = "/";

    private final NetworkConfigurationProvider networkConfigurationProvider;

    private final DialogHandler dialogHandler;

    private final SambaWorkgroupProvider sambaWorkgroupProvider;

    /**
     * The credentials entered by the user, by host name,
     * used instead of the configured ones until the application is closed.
     */
    private final Map<String, SambaCredentials> credentialsCache = new ConcurrentHashMap<>();

    public SambaConnectionFactory(final NetworkConfigurationProvider networkConfigurationProvider,
                                  final DialogHandler dialogHandler,
                                  final SambaWorkgroupProvider sambaWorkgroupProvider) {
        this.networkConfigurationProvider = networkConfigurationProvider;
        this.dialogHandler = dialogHandler;
        this.sambaWorkgroupProvider = sambaWorkgroupProvider;
    }

    /**
     * Create a connection to a samba shared folder.
     * <p>
     * If the access is denied, the user is asked for the username and password, and the connection is retried
     * with them until it succeeds or the user cancels.
     * If the user cancels, an {@link AbortActionException} is thrown, so the action is aborted without an error.
     *
     * @param key the connection key, as "host/share" (see {@link #connectionKey(String, String)})
     * @return the connection
     * @throws Exception if the connection fails
     */
    @Override
    public SambaConnection create(String key) throws Exception {
        String hostName = hostName(key);
        String shareName = shareName(key);
        SambaConfiguration hostConfiguration = getSambaHostConfiguration(hostName);
        SambaFolderConfiguration shareConfiguration = hostConfiguration.findShare(shareName)
            .orElseGet(() -> SambaFolderConfiguration.share(shareName));

        return runWithCredentials(hostName,
            hostConfiguration,
            hostConfiguration.description(shareConfiguration.name()),
            credentialsConfiguration -> connect(key, credentialsConfiguration, shareConfiguration));
    }

    /**
     * Run a samba action on a host, asking the user for other credentials while the access is denied.
     * <p>
     * The action is run with the credentials entered by the user for that host, if any, otherwise with the configured ones.
     * If the access is denied, the user is asked for the username and password, and the action is retried
     * with them until it succeeds or the user cancels.
     * If the user cancels, an {@link AbortActionException} is thrown, so the action is aborted without an error.
     *
     * @param hostName          the host name
     * @param hostConfiguration the host configuration
     * @param location          the accessed location, shown to the user when asking for credentials
     * @param action            the action, run with the host configuration having the credentials to use
     * @param <T>               the action result type
     * @return the action result
     * @throws Exception if the action fails
     */
    public <T> T runWithCredentials(String hostName,
                                    SambaConfiguration hostConfiguration,
                                    String location,
                                    SambaAction<T> action) throws Exception {
        return runWithCredentialsConfiguration(hostName,
            withEnteredCredentials(hostName, hostConfiguration),
            location,
            action);
    }

    /**
     * Run a samba action on a host with the given credentials, retrying it with the credentials entered by the user
     * if the access is denied.
     *
     * @param hostName                 the host name
     * @param credentialsConfiguration the host configuration having the credentials to use
     * @param location                 the accessed location, shown to the user when asking for credentials
     * @param action                   the action, run with the host configuration having the credentials to use
     * @param <T>                      the action result type
     * @return the action result
     * @throws Exception if the action fails
     */
    private <T> T runWithCredentialsConfiguration(String hostName,
                                                  SambaConfiguration credentialsConfiguration,
                                                  String location,
                                                  SambaAction<T> action) throws Exception {
        try {
            return action.run(credentialsConfiguration);
        } catch (Exception ex) {
            if (isNoRouteToHost(ex)) {
                throw new StreamerException(ex, "samba-error-no-route-to-host", credentialsConfiguration.host());
            }
            if (!(ex instanceof SMBApiException smbApiException) || !isAccessDenied(smbApiException)) {
                throw ex;
            }
            LOG.info("Access denied to {} as {}",
                location,
                isBlank(credentialsConfiguration.username()) ? "guest" : credentialsConfiguration.username());
            SambaCredentials credentials = askCredentials(hostName, credentialsConfiguration, location)
                .orElseThrow(() -> new AbortActionException("Samba credentials cancelled by user", ex));
            return runWithCredentialsConfiguration(hostName,
                credentialsConfiguration.withCredentials(credentials.domain(), credentials.username(), credentials.password()),
                location,
                action);
        }
    }

    /**
     * Use the credentials entered by the user for a samba host, if any, instead of the configured ones.
     *
     * @param hostName          the host name
     * @param hostConfiguration the host configuration
     * @return the host configuration, with the entered credentials
     */
    public SambaConfiguration withEnteredCredentials(String hostName, SambaConfiguration hostConfiguration) {
        return ofNullable(credentialsCache.get(hostName))
            .map(credentials -> hostConfiguration.withCredentials(credentials.domain(), credentials.username(), credentials.password()))
            .orElse(hostConfiguration);
    }

    /**
     * Check if a samba error means the credentials are not accepted, so other ones should be asked:
     * the access is denied, the logon failed, or the account is disabled
     * (e.g. the guest account, on servers not allowing guest access).
     *
     * @param ex the samba error
     * @return true if the user should be asked for credentials
     */
    private boolean isAccessDenied(SMBApiException ex) {
        return ex.getStatus() == STATUS_ACCESS_DENIED ||
            ex.getStatus() == STATUS_LOGON_FAILURE ||
            ex.getStatus() == STATUS_ACCOUNT_DISABLED;
    }

    /**
     * Check if a connection error means the samba server cannot be reached from this network
     * (e.g. the server is off, or it is in another network).
     *
     * @param ex the connection error
     * @return true if there is no route to the samba server
     */
    public boolean isNoRouteToHost(Throwable ex) {
        return indexOfType(ex, NoRouteToHostException.class) >= 0;
    }

    /**
     * Ask the user for the credentials to access a samba host or share, in a dialog.
     * <p>
     * If other credentials were entered for the same host, before or while waiting for another credentials dialog
     * to be closed, they are used instead, without showing the dialog.
     * The domain to start with is the configured one, if any, otherwise the current workgroup.
     *
     * @param hostName            the host name
     * @param deniedConfiguration the host configuration whose credentials were denied
     * @param location            the accessed location
     * @return the credentials, or empty if the user cancelled
     */
    private Optional<SambaCredentials> askCredentials(String hostName,
                                                      SambaConfiguration deniedConfiguration,
                                                      String location) {
        Optional<SambaCredentials> newCredentials = getCredentialsForConfiguration(hostName, deniedConfiguration)
            .or(() -> dialogHandler.lockAndShowDialog(applicationFrame,
                    SAMBA_CREDENTIALS_DIALOG,
                    () -> getCredentialsForConfiguration(hostName, deniedConfiguration).isPresent() ?
                        Optional.empty() :
                        Optional.of(new SambaCredentialsModel(location, getDomain(deniedConfiguration), deniedConfiguration.username())))
                .map(SambaCredentialsModel::toCredentials)
                .or(() -> getCredentialsForConfiguration(hostName, deniedConfiguration)));
        newCredentials.ifPresent(entered -> credentialsCache.put(hostName, entered));
        return newCredentials;
    }

    private String getDomain(SambaConfiguration sambaConfiguration) {
        return ofNullable(sambaConfiguration.domain())
            .filter(not(String::isBlank))
            .or(sambaWorkgroupProvider::currentWorkgroup)
            .orElse(null);
    }

    /**
     * @param hostName            the host name
     * @param sambaConfiguration the host configuration whose credentials were denied
     * @return the credentials entered for the host, if they are not the denied ones
     */
    private Optional<SambaCredentials> getCredentialsForConfiguration(String hostName,
                                                                      SambaConfiguration sambaConfiguration) {
        return ofNullable(credentialsCache.get(hostName))
            .filter(credentials -> !credentials.isUsedBy(sambaConfiguration));
    }

    private SambaConnection connect(String key,
                                    SambaConfiguration hostConfiguration,
                                    SambaFolderConfiguration shareConfiguration) throws Exception {
        int connectionTimeout = hostConfiguration.connectionTimeoutOrDefault();

        // no socket timeout, as the pooled connections are idle between requests,
        // and an idle socket timeout breaks the connection, which smbj then fails to log off.
        // The requests are still limited by the read, write and transact timeouts.
        SmbConfig smbConfig = SmbConfig.builder()
            .withTimeout(connectionTimeout, SECONDS)
            .build();
        SMBClient client = new SMBClient(smbConfig);
        try {
            int port = hostConfiguration.portOrDefault();

            Session session = client
                .connect(hostConfiguration.host(), port)
                .authenticate(createAuthenticationContext(hostConfiguration));
            LOG.info("Connected to samba server {}", hostConfiguration.host());

            Share share = session.connectShare(shareConfiguration.name());
            if (!(share instanceof DiskShare diskShare)) {
                share.close();
                throw new StreamerException("Samba share is not a disk share: " +
                    hostConfiguration.description(shareConfiguration.name()));
            }
            return new SambaConnection(key, client, diskShare, shareConfiguration.remoteDir());
        } catch (Exception ex) {
            client.close();
            throw ex;
        }
    }

    public AuthenticationContext createAuthenticationContext(SambaConfiguration hostConfiguration) {
        return isBlank(hostConfiguration.username()) ?
            AuthenticationContext.guest() :
            new AuthenticationContext(hostConfiguration.username(),
                ofNullable(hostConfiguration.password())
                    .map(String::toCharArray)
                    .orElseGet(() -> new char[0]),
                hostConfiguration.domain());
    }

    public String connectionKey(String hostName, String shareName) {
        return hostName + KEY_SEPARATOR + shareName;
    }

    public String hostName(String connectionKey) {
        return connectionKey.split(KEY_SEPARATOR)[0];
    }

    public String shareName(String connectionKey) {
        String[] parts = connectionKey.split(KEY_SEPARATOR);
        return parts.length > 1 ? parts[1] : "";
    }

    @Override
    public boolean validateConnection(SambaConnection sambaConnection) {
        return sambaConnection.isConnected();
    }

    @Override
    public void destroyConnection(SambaConnection sambaConnection) {
        sambaConnection.disconnect();
    }

    public SambaConfiguration getSambaHostConfiguration(String hostName) {
        return networkConfigurationProvider.getHost(hostName, SambaConfiguration.class)
            .orElseThrow(() -> new StreamerException("Samba host configuration not found: " + hostName));
    }

    /**
     * A samba action, run with the credentials of a host configuration.
     *
     * @param <T> the action result type
     */
    @FunctionalInterface
    public interface SambaAction<T> {

        /**
         * @param hostConfiguration the host configuration, with the credentials to use
         * @return the action result
         * @throws Exception if the action fails
         */
        T run(SambaConfiguration hostConfiguration) throws Exception;
    }
}
