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


package org.cosinus.streamer.ftp.credentials;

import org.cosinus.streamer.ftp.model.FtpConfiguration;

import java.util.Objects;

/**
 * FTP credentials entered by the user.
 *
 * @param username the username
 * @param password the password
 */
public record FtpCredentials(String username, String password) {

    /**
     * Check if these credentials are the ones of an ftp host configuration.
     *
     * @param hostConfiguration the ftp host configuration
     * @return true if the username and the password are the same
     */
    public boolean isUsedBy(FtpConfiguration hostConfiguration) {
        return Objects.equals(username, hostConfiguration.username()) &&
            Objects.equals(password, hostConfiguration.password());
    }
}
