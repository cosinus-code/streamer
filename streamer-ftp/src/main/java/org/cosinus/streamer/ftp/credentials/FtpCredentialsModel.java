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

import org.cosinus.swing.ui.UIModel;

import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * The model of the ftp credentials dialog.
 */
public class FtpCredentialsModel implements UIModel {

    public static final String FTP_CREDENTIALS_DIALOG = "ftpCredentialsDialog.json";

    public static final String FTP_LOCATION = "ftp-location";

    public static final String FTP_USERNAME = "ftp-username";

    public static final String FTP_PASSWORD = "ftp-password";

    private static final Set<String> KEYS = Set.of(
        FTP_LOCATION,
        FTP_USERNAME,
        FTP_PASSWORD);

    private final String location;

    private String username;

    private String password;

    /**
     * @param location the host location, as ftp://host
     * @param username the username to start with, if any, otherwise the name of the current logged user is used
     */
    public FtpCredentialsModel(final String location, final String username) {
        this.location = location;
        this.username = isBlank(username) ? System.getProperty("user.name") : username;
    }

    @Override
    public Set<String> keys() {
        return KEYS;
    }

    @Override
    public void putValue(String key, Object value) {
        String text = ofNullable(value)
            .map(Object::toString)
            .orElse(null);
        switch (key) {
            case FTP_USERNAME -> username = text;
            case FTP_PASSWORD -> password = text;
            default -> {
            }
        }
    }

    @Override
    public Object getValue(String key) {
        return switch (key) {
            case FTP_LOCATION -> location;
            case FTP_USERNAME -> username;
            case FTP_PASSWORD -> password;
            default -> null;
        };
    }

    public FtpCredentials toCredentials() {
        return new FtpCredentials(username, password);
    }
}
