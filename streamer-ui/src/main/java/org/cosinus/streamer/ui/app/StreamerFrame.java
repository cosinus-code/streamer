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

package org.cosinus.streamer.ui.app;

import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.*;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.preference.Preferences;
import org.springframework.stereotype.Component;

import java.awt.*;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.util.Arrays.stream;
import static org.cosinus.streamer.ui.action.execute.find.FindActionModel.findLastStreamerAndConsume;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ADDRESS_BAR;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SHOW_LEFT_VIEW;
import static org.cosinus.streamer.ui.view.PanelLocation.LEFT;

@Component
public class StreamerFrame extends SwingApplicationFrame {

    private MainSplit split;

    private final StreamerViewHandler streamerViewHandler;

    private final Preferences preferences;

    private final AddressBar addressBar;

    private final ActionExecutors actionExecutors;

    public StreamerFrame(final StreamerViewHandler streamerViewHandler,
                         final Preferences preferences,
                         final AddressBar addressBar,
                         final ActionExecutors actionExecutors) {
        this.streamerViewHandler = streamerViewHandler;
        this.preferences = preferences;
        this.addressBar = addressBar;
        this.actionExecutors = actionExecutors;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        split = new MainSplit();
        split.initComponent();

        addressBar.initComponents();

        stream(PanelLocation.values())
            .forEach(this::addStreamerPanel);
        setVisibleSidebar(preferences.booleanPreference(SHOW_LEFT_VIEW));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().removeAll();
        getContentPane().add(split, CENTER);

        if (preferences.booleanPreference(ADDRESS_BAR)) {
            getContentPane().add(addressBar, NORTH);
        }
    }

    private void addStreamerPanel(PanelLocation location) {
        split.add(streamerViewHandler.createStreamerPanel(location),
            location.toString());
    }

    public void setVisibleSidebar(boolean visible) {
        streamerViewHandler.getPanel(LEFT)
            .ifPresent(panel -> panel.setVisible(visible));
        split.setVisibleDivider(visible);
    }

    @Override
    public void loadContent() {
//        stream(PanelLocation.values())
//            .forEach(location -> actionExecutors.execute(new LoadActionModel(
//                location, null, null)));
        stream(PanelLocation.values())
            .forEach(location -> actionExecutors
                .execute(findLastStreamerAndConsume(location, streamerToLoad -> actionExecutors
                    .execute(new LoadActionModel(location, streamerToLoad, null)))));
    }

    public StreamerView getCurrentView() {
        return streamerViewHandler.getCurrentView();
    }

    @Override
    public void translate() {
        super.translate();

        split.translate();
        streamerViewHandler.getPanels().forEach(StreamerPanel::translate);
    }
}
