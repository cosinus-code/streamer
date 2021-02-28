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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.cosinus.streamer.api.error.StreamerException;
import org.springframework.stereotype.Component;

/**
 * Pool for {@link FTPClient} objects
 */
@Component
public class FtpClientPool extends GenericKeyedObjectPool<String, FtpClient> {

    public FtpClientPool(FtpClientFactory ftpClientFactory,
                         FtpClientPoolConfig ftpClientPoolConfig) {
        super(ftpClientFactory, ftpClientPoolConfig);
    }

    public FtpClient borrowObject(String name) {
        try {
            FtpClient ftpClient = super.borrowObject(name);
            ftpClient.setFtpClientPool(this);
            return ftpClient;
        } catch (Exception e) {
            throw new StreamerException(e);
        }
    }
}
