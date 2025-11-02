/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.ui.menu;

import org.cosinus.swing.file.Application;
import org.cosinus.swing.ui.UIModel;

import java.util.Collection;
import java.util.Set;

import static java.util.Optional.ofNullable;

public class ExecuteStreamerModel implements UIModel {

    private static final String DEFAULT_APPLICATION = "default-application";

    private static final String COMPATIBLE_APPLICATIONS = "compatible-applications";

    private static final String SET_AS_DEFAULT_APPLICATION = "set-as-default-application";

    private static final Set<String> KEYS = Set.of(
        DEFAULT_APPLICATION,
        COMPATIBLE_APPLICATIONS,
        SET_AS_DEFAULT_APPLICATION);

    private final Collection<Application> compatibleApplications;

    private final Application defaultApplication;

    private Application selectedApplication;

    private boolean setAsDefault;

    public ExecuteStreamerModel(final Collection<Application> compatibleApplications,
                                final Application defaultApplication) {
        this.compatibleApplications = compatibleApplications;
        this.defaultApplication = defaultApplication;
    }

    @Override
    public Set<String> keys() {
        return KEYS;
    }

    @Override
    public void putValue(String key, Object value) {
        if (key.equals(COMPATIBLE_APPLICATIONS)) {
            selectedApplication = (Application) value;
        }
        if (key.equals(SET_AS_DEFAULT_APPLICATION)) {
            ofNullable(value)
                .filter(Boolean.class::isInstance)
                .map(Boolean.class::cast)
                .ifPresent(this::setSetAsDefault);
        }
    }

    @Override
    public Object getValue(String key) {
        if (key.equals(COMPATIBLE_APPLICATIONS)) {
            return compatibleApplications
                .stream()
                .findFirst()
                .orElse(null);
        }
        if (key.equals(DEFAULT_APPLICATION)) {
            return defaultApplication;
        }
        return null;
    }

    @Override
    public Object[] getValues(String key) {
        if (key.equals(COMPATIBLE_APPLICATIONS)) {
            return compatibleApplications.toArray(Application[]::new);
        }
        if (key.equals(DEFAULT_APPLICATION)) {
            return new Application[]{defaultApplication};
        }
        return null;
    }

    public Application getSelectedApplication() {
        return selectedApplication;
    }

    public boolean isSetAsDefault() {
        return setAsDefault;
    }

    public void setSetAsDefault(boolean setAsDefault) {
        this.setAsDefault = setAsDefault;
    }
}
