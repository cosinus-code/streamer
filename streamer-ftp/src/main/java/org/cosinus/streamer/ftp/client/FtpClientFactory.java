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

package org.cosinus.streamer.ftp.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
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

/**
 * Factory for {@link FTPClient} objects
 */
@Component
public class FtpClientFactory extends BaseKeyedPooledObjectFactory<String, FtpClient> {

    private static final Logger LOG = LogManager.getLogger(FtpClientFactory.class);

    private final FtpModel ftpModel;

    public FtpClientFactory(FtpModelProvider ftpModelProvider) {
        this.ftpModel = ftpModelProvider.getFtpModel();
    }

    public FtpClientModel getFtpClientConfig(String name) {
        return ftpModel.findFtpClientConfiguration(name)
            .orElseThrow(() -> new StreamerException("FTP client configuration not found: " + name));
    }

    @Override
    public FtpClient create(String name) throws IOException {
        FtpClientModel ftpConfig = getFtpClientConfig(name);

        FtpClient ftpClient = new FtpClient(name);
        Optional.ofNullable(ftpConfig.getEncoding())
            .ifPresent(ftpClient::setControlEncoding);
        ftpClient.setConnectTimeout(ftpConfig.getConnectionTimeout() * 1000);

        ftpClient.connect(ftpConfig.getHost(),
                          ftpConfig.getPort());
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            ftpClient.disconnect();
            LOG.warn("FTP server {} refused connection: {}",
                     ftpConfig.getHost(),
                     replyCode);
            return null;
        }
        LOG.info("Connected to FTP server " + ftpConfig.getHost());
        LOG.info(ftpClient.getReplyString());

        if (!ftpClient.login(ftpConfig.getUsername(),
                             ftpConfig.getPassword())) {
            LOG.warn("FTP login to server {} failed for username {}",
                     ftpConfig.getHost(),
                     ftpConfig.getUsername());
            return null;
        }

        if (ftpConfig.isPassiveMode()) {
            ftpClient.enterLocalPassiveMode();
        }

        if (!StringUtils.isEmpty(ftpConfig.getRemoteDir())) {
            ftpClient.changeWorkingDirectory(ftpConfig.getRemoteDir());
        }

        return ftpClient;
    }

    @Override
    public PooledObject<FtpClient> wrap(FtpClient ftpClient) {
        return new DefaultPooledObject<>(ftpClient);
    }

    @Override
    public boolean validateObject(String name, PooledObject<FtpClient> pooledFtpClient) {
        return Optional.ofNullable(pooledFtpClient)
            .map(PooledObject::getObject)
            .map(this::runValidateCommand)
            .map(FTPReply::isPositiveCompletion)
            .orElse(false);
    }

    protected int runValidateCommand(FTPClient ftpClient) {
        try {
            return ftpClient.noop();
        } catch (IOException e) {
            LOG.error("FTP client validation failed", e);
            return 0;
        }
    }

    @Override
    public void destroyObject(String name, PooledObject<FtpClient> pooledFtpClient) {
        Optional.ofNullable(pooledFtpClient)
            .map(PooledObject::getObject)
            .ifPresent(ftpClient -> {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                    }
                } catch (IOException io) {
                    LOG.error("FTP logout failed", io);
                } finally {
                    try {
                        ftpClient.disconnect();
                    } catch (IOException io) {
                        LOG.error("FTP disconnect failed", io);
                    }
                }
            });
    }
}
