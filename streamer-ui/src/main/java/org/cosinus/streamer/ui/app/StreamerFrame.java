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

import org.cosinus.streamer.ui.view.*;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.context.SwingInjector;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Optional;

import static java.awt.BorderLayout.CENTER;
import static java.util.Arrays.stream;

@Component
public class StreamerFrame extends SwingApplicationFrame {

    private final SwingInjector swingInjector;

    private MainSplit split;

    private final StreamerViewHandler streamerViewHandler;

    public StreamerFrame(SwingInjector swingInjector,
                         StreamerViewHandler streamerViewHandler) {
        this.swingInjector = swingInjector;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        split = swingInjector.inject(MainSplit.class);
        split.initComponent();

        stream(PanelLocation.values())
            .forEach(location -> split.add(streamerViewHandler.createStreamerPanel(location),
                                           location.toString()));

        setLayout(new BorderLayout());
        add(split, CENTER);
    }

    public void setVisibleSidebar(boolean visible) {
        Optional.ofNullable(streamerViewHandler.getPanel(PanelLocation.LEFT))
            .ifPresent(panel -> panel.setVisible(visible));
        split.setVisible(visible);
    }

    @Override
    public void initContent() {
        split.initContent();
        streamerViewHandler.getPanels().forEach(StreamerPanel::initContent);
    }

    public void updateForm() {
        streamerViewHandler.getPanels().forEach(StreamerPanel::updateForm);
    }

    public StreamerView getCurrentView() {
        return streamerViewHandler.getCurrentView();
    }

    public void setCurrentLocation(PanelLocation currentLocation) {
        streamerViewHandler.setCurrentLocation(currentLocation);
    }

    @Override
    public void translate() {
        split.translate();
        streamerViewHandler.getPanels().forEach(StreamerPanel::translate);
    }
}
