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

package org.cosinus.streamer.samba.credentials;

import org.cosinus.swing.ui.UIModel;

import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * The model of the samba credentials dialog.
 */
public class SambaCredentialsModel implements UIModel {

    public static final String SAMBA_CREDENTIALS_DIALOG = "sambaCredentialsDialog.json";

    public static final String SAMBA_LOCATION = "samba-location";

    public static final String SAMBA_DOMAIN = "samba-domain";

    public static final String SAMBA_USERNAME = "samba-username";

    public static final String SAMBA_PASSWORD = "samba-password";

    private static final Set<String> KEYS = Set.of(
        SAMBA_LOCATION,
        SAMBA_DOMAIN,
        SAMBA_USERNAME,
        SAMBA_PASSWORD);

    private final String location;

    private String domain;

    private String username;

    private String password;

    /**
     * @param location the share location, as \\host\share
     * @param domain   the domain to start with, if any
     * @param username the username to start with, if any, otherwise the name of the current logged user is used
     */
    public SambaCredentialsModel(final String location, final String domain, final String username) {
        this.location = location;
        this.domain = domain;
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
            case SAMBA_DOMAIN -> domain = text;
            case SAMBA_USERNAME -> username = text;
            case SAMBA_PASSWORD -> password = text;
            default -> {
            }
        }
    }

    @Override
    public Object getValue(String key) {
        return switch (key) {
            case SAMBA_LOCATION -> location;
            case SAMBA_DOMAIN -> domain;
            case SAMBA_USERNAME -> username;
            case SAMBA_PASSWORD -> password;
            default -> null;
        };
    }

    public SambaCredentials toCredentials() {
        return new SambaCredentials(isBlank(domain) ? null : domain, username, password);
    }
}
