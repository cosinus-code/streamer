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

package org.cosinus.streamer.samba.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.cosinus.streamer.network.NetworkHost;

import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Samba (SMB) server info, with its shared folders
 *
 * @param name              the host name, used as root of the streamer paths
 * @param host              the samba server host name or ip
 * @param port              the samba server port (default 445)
 * @param domain            the authentication domain (workgroup)
 * @param username          the username; if empty, the guest account is used
 * @param password          the password
 * @param connectionTimeout the connection timeout in seconds (default 30)
 * @param folders           the shared folders of the server (empty if not provided)
 */
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName(SambaConfiguration.SAMBA_HOST_TYPE)
public record SambaConfiguration(String name, String host, Integer port, String domain,
                                 String username, String password, Integer connectionTimeout,
                                 List<SambaFolderConfiguration> folders) implements NetworkHost {

    public static final String SAMBA_HOST_TYPE = "samba";

    public static final int DEFAULT_PORT = 445;

    public static final int DEFAULT_CONNECTION_TIMEOUT = 30;

    public SambaConfiguration {
        if (folders == null) {
            folders = List.of();
        }
    }

    @Override
    public String type() {
        return SAMBA_HOST_TYPE;
    }

    /**
     * Find a shared folder by name, ignoring the case, as samba share names are case-insensitive.
     *
     * @param shareName the share name
     * @return the shared folder, if configured
     */
    public Optional<SambaFolderConfiguration> findShare(String shareName) {
        return folders.stream()
            .filter(share -> share.name().equalsIgnoreCase(shareName))
            .findFirst();
    }

    /**
     * @return the configured port, or the default samba port if not set
     */
    public int portOrDefault() {
        return port != null ? port : DEFAULT_PORT;
    }

    /**
     * @return the configured connection timeout in seconds, or the default one if not set
     */
    public int connectionTimeoutOrDefault() {
        return connectionTimeout != null ? connectionTimeout : DEFAULT_CONNECTION_TIMEOUT;
    }

    @Override
    public SambaConfiguration withName(String newName) {
        return new SambaConfiguration(newName, host, port, domain, username, password, connectionTimeout, folders);
    }

    public SambaConfiguration withCredentials(String newDomain, String newUsername, String newPassword) {
        return new SambaConfiguration(name, host, port, newDomain, newUsername, newPassword, connectionTimeout, folders);
    }

    public SambaConfiguration withFolders(List<SambaFolderConfiguration> newFolders) {
        return new SambaConfiguration(name, host, port, domain, username, password, connectionTimeout, newFolders);
    }

    public String description() {
        return "\\\\" + host;
    }

    public String description(String share) {
        return description() + "\\" + share;
    }
}
