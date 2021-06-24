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

package org.cosinus.streamer.ui.action.preference;

import org.cosinus.streamer.ui.app.StreamerFrame;
import org.cosinus.swing.action.ChangePreferenceAction;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.preference.StreamerPreferences.SHOW_LEFT_VIEW;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class ChangeShowLeftViewPreferenceAction extends ChangePreferenceAction {

    private static final String MENU_SHOW_LEFT_VIEW = "menu-view-left-view";

    public ChangeShowLeftViewPreferenceAction(Preferences preferences,
                                              PreferencesProvider preferencesProvider) {
        super(preferences, preferencesProvider);
    }

    @Override
    protected String getPreferenceName() {
        return SHOW_LEFT_VIEW;
    }

    @Override
    protected Object getPreferenceNewValue() {
        return !preferences.booleanPreference(SHOW_LEFT_VIEW);
    }

    @Override
    protected void apply() {
        StreamerFrame streamerFrame = (StreamerFrame) applicationFrame;
        streamerFrame.setVisibleSidebar(preferences.booleanPreference(SHOW_LEFT_VIEW));
    }

    @Override
    public String getId() {
        return MENU_SHOW_LEFT_VIEW;
    }

}
