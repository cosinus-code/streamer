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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.remote.AbstractConnectionFactory;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.ftp.model.FtpConfigurationProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.apache.commons.net.ftp.FTPReply.isPositiveCompletion;

@Component
public class FtpConnectionFactory extends AbstractConnectionFactory<String, FtpConnection, FTPFile> {

    private static final Logger LOG = LogManager.getLogger(FtpConnectionFactory.class);
    private final Map<String, FtpConfiguration> ftpConfigurationsMap;

    public FtpConnectionFactory(final FtpConfigurationProvider ftpConfigurationProvider) {
        this.ftpConfigurationsMap = ftpConfigurationProvider.getConnectionModelsMap();
    }

    @Override
    public FtpConnection create(String name) throws Exception {
        FtpConfiguration ftpConfiguration = getFtpConnectionConfig(name);

        FtpConnection ftpConnection = new FtpConnection(name);
        ftpConnection.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
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

        if (!ftpConnection.login(ftpConfiguration.username(), ftpConfiguration.password())) {
            LOG.warn("FTP login to server {} failed for username {}",
                ftpConfiguration.host(),
                ftpConfiguration.username());
            return null;
        }

        if (ftpConfiguration.passiveMode()) {
            ftpConnection.enterLocalPassiveMode();
        }

        if (!StringUtils.isEmpty(ftpConfiguration.remoteDir())) {
            ftpConnection.changeWorkingDirectory(ftpConfiguration.remoteDir());
        }

        return ftpConnection;
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

    public FtpConfiguration getFtpConnectionConfig(String name) {
        return ofNullable(ftpConfigurationsMap.get(name))
            .orElseThrow(() -> new StreamerException("FTP connection configuration not found: " + name));
    }
}
