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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.ui.app.StreamerFrame;
import org.cosinus.swing.action.ChangePreferenceAction;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_L;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ADDRESS_BAR;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.image.icon.IconProvider.ICON_VIEW_ADDRESS_BAR;

/**
 * Show or hide the address bar.
 */
@Component
public class ShowAddressBarAction extends ChangePreferenceAction {

    private static final String MENU_SHOW_ADDRESS_BAR = "menu-view-address-bar";

    private final ApplicationUIHandler uiHandler;

    public ShowAddressBarAction(Preferences preferences,
                                PreferencesProvider preferencesProvider,
                                ApplicationUIHandler uiHandler) {
        super(preferences, preferencesProvider);
        this.uiHandler = uiHandler;
    }

    @Override
    protected String getPreferenceName() {
        return ADDRESS_BAR;
    }

    @Override
    protected Object getPreferenceNewValue() {
        return !preferences.booleanPreference(ADDRESS_BAR);
    }

    @Override
    protected void apply() {
        StreamerFrame streamerFrame = (StreamerFrame) applicationFrame;
        streamerFrame.setVisibleAddressBar(preferences.booleanPreference(ADDRESS_BAR));
    }

    @Override
    public String getIconName() {
        return ICON_VIEW_ADDRESS_BAR;
    }

    @Override
    public String getId() {
        return MENU_SHOW_ADDRESS_BAR;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_L, uiHandler.getControlDownKeyMask() | SHIFT_DOWN_MASK));
    }

    @Override
    public Optional<Boolean> getSelectedState() {
        return Optional.of(preferences.booleanPreference(ADDRESS_BAR));
    }
}
