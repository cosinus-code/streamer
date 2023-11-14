/*
 * Copyright 2020 Cosinus Software
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
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.ftp.model.FtpClientModel;
import org.cosinus.streamer.ftp.model.FtpModel;
import org.cosinus.streamer.ftp.model.FtpModelProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class FtpConnectionFactory extends BaseKeyedPooledObjectFactory<String, FtpConnection> {

    private static final Logger LOG = LogManager.getLogger(FtpConnectionFactory.class);
    private final FtpModel ftpModel;

    public FtpConnectionFactory(final FtpModelProvider ftpModelProvider) {
        this.ftpModel = ftpModelProvider.getFtpModel();
    }

    @Override
    public FtpConnection create(String name) throws Exception
    {
        FtpClientModel ftpConfig = getFtpConnectionConfig(name);

        FtpConnection ftpConnection = new FtpConnection(name);
        ftpConnection.configure(new FTPClientConfig(FTPClientConfig.SYST_UNIX));
        ofNullable(ftpConfig.getEncoding())
            .ifPresent(ftpConnection::setControlEncoding);
        ftpConnection.setConnectTimeout(ftpConfig.getConnectionTimeout() * 1000);

        ftpConnection.connect(ftpConfig.getHost(), ftpConfig.getPort());
        int replyCode = ftpConnection.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            ftpConnection.disconnect();
            LOG.warn("FTP server {} refused connection: {}",
                ftpConfig.getHost(),
                replyCode);
            return null;
        }
        LOG.info("Connected to FTP server " + ftpConfig.getHost());
        LOG.info(ftpConnection.getReplyString());

        if (!ftpConnection.login(ftpConfig.getUsername(), ftpConfig.getPassword())) {
            LOG.warn("FTP login to server {} failed for username {}",
                ftpConfig.getHost(),
                ftpConfig.getUsername());
            return null;
        }

        if (ftpConfig.isPassiveMode()) {
            ftpConnection.enterLocalPassiveMode();
        }

        if (!StringUtils.isEmpty(ftpConfig.getRemoteDir())) {
            ftpConnection.changeWorkingDirectory(ftpConfig.getRemoteDir());
        }

        return ftpConnection;
    }

    @Override
    public PooledObject<FtpConnection> wrap(FtpConnection ftpConnection)
    {
        return new DefaultPooledObject<>(ftpConnection);
    }

    @Override
    public boolean validateObject(String name, PooledObject<FtpConnection> pooledFtpConnection) {
        return Optional.ofNullable(pooledFtpConnection)
            .map(PooledObject::getObject)
            .map(this::runValidateCommand)
            .map(FTPReply::isPositiveCompletion)
            .orElse(false);
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
    public void destroyObject(String name, PooledObject<FtpConnection> ftpConnection) {
        ofNullable(ftpConnection)
            .map(PooledObject::getObject)
            .ifPresent(connection -> {
                try {
                    if (connection.isConnected()) {
                        connection.logout();
                    }
                } catch (IOException io) {
                    LOG.error("FTP logout failed", io);
                } finally {
                    try {
                        connection.disconnect();
                    } catch (IOException io) {
                        LOG.error("FTP disconnect failed", io);
                    }
                }
            });
    }

    public FtpClientModel getFtpConnectionConfig(String name) {
        return ftpModel.findFtpClientConfiguration(name)
            .orElseThrow(() -> new StreamerException("FTP connection configuration not found: " + name));
    }
}
