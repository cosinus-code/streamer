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

package org.cosinus.streamer.ftp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.cosinus.streamer.network.NetworkHost;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cosinus.streamer.ftp.FtpMainStreamer.FTP_PROTOCOL;

/**
 * FTP connection info.
 * <p>
 * The port, the connection timeout, the keep alive timeout and the encoding have default values, if not set.
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName(FtpConfiguration.FTP_HOST_TYPE)
public record FtpConfiguration(String name, String host, int port, String username, String password,
                               int connectionTimeout, boolean passiveMode, String remoteDir, String localDir,
                               String keepAliveCommand, int keepAliveTimeout, String encoding) implements NetworkHost {

    public static final String FTP_HOST_TYPE = "ftp";

    public static final int DEFAULT_PORT = 21;

    public static final int DEFAULT_CONNECTION_TIMEOUT = 30;

    public static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 90;

    public static final String DEFAULT_ENCODING = "UTF-8";

    public FtpConfiguration {
        port = port > 0 ? port : DEFAULT_PORT;
        connectionTimeout = connectionTimeout > 0 ? connectionTimeout : DEFAULT_CONNECTION_TIMEOUT;
        keepAliveTimeout = keepAliveTimeout > 0 ? keepAliveTimeout : DEFAULT_KEEP_ALIVE_TIMEOUT;
        encoding = encoding != null ? encoding : DEFAULT_ENCODING;
    }

    /**
     * @param name the host name, also used as the host address
     * @return a new ftp host configuration, with the default values
     */
    public static FtpConfiguration host(String name) {
        return new FtpConfiguration(name, name, 0, null, null, 0, false, null, null, null, 0, null);
    }

    public boolean hasCredentials() {
        return isBlank(username()) || isBlank(password());
    }

    @Override
    public String type() {
        return FTP_HOST_TYPE;
    }

    public FtpConfiguration withCredentials(String newUsername, String newPassword) {
        return new FtpConfiguration(name, host, port, newUsername, newPassword, connectionTimeout, passiveMode,
            remoteDir, localDir, keepAliveCommand, keepAliveTimeout, encoding);
    }

    /**
     * @return the host location, as ftp://host, followed by the port if it is not the default one
     */
    public String description() {
        return FTP_PROTOCOL + host + (port == DEFAULT_PORT ? "" : ":" + port);
    }

    @Override
    public FtpConfiguration withName(String newName) {
        return new FtpConfiguration(newName, host, port, username, password, connectionTimeout, passiveMode,
            remoteDir, localDir, keepAliveCommand, keepAliveTimeout, encoding);
    }
}
