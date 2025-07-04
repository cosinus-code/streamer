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

import org.cosinus.streamer.file.system.Application;
import org.cosinus.swing.ui.UIModel;

import java.util.Set;

public class ExecuteWithUiModel implements UIModel {

    private final Set<Application> compatibleApplications;

    private Application selectedApplication;

    public ExecuteWithUiModel(final Set<Application> compatibleApplications) {
        this.compatibleApplications = compatibleApplications;
    }

    @Override
    public Set<String> keys() {
        return Set.of("applications");
    }

    @Override
    public void putValue(String key, Object value) {
        selectedApplication = (Application) value;
    }

    @Override
    public Object getValue(String key) {
        return compatibleApplications
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public Object[] getValues(String key) {
        return compatibleApplications.toArray(Application[]::new);
    }

    public Application getSelectedApplication() {
        return selectedApplication;
    }
}
