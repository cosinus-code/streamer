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

package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.view.table.details.DetailView;
import org.cosinus.swing.context.SpringSwingComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.cosinus.streamer.ui.view.PanelLocation.LEFT;

/**
 * Handler for data views
 */
@SpringSwingComponent
public class StreamerViewHandler {

    private PanelLocation currentLocation = LEFT;

    private final Map<PanelLocation, StreamerPanel> panelsMap = new HashMap<>();

    private final StreamerHandler streamerHandler;

    public StreamerViewHandler(StreamerHandler streamerHandler) {
        this.streamerHandler = streamerHandler;
    }

    public PanelLocation getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(PanelLocation currentLocation) {
        this.currentLocation = currentLocation;
        setActiveView(currentLocation);
    }

    public StreamerView getCurrentView() {
        return Optional.ofNullable(panelsMap.get(currentLocation))
            .map(StreamerPanel::getView)
            .orElse(null);
    }

    private void setActiveView(PanelLocation currentLocation) {
        panelsMap.forEach((location, panel) -> panel.getView().setActive(location == currentLocation));
    }

    public StreamerView createDataView(PanelLocation location) {
        return new DetailView(location);
    }

    public StreamerPanel createStreamerPanel(PanelLocation location) {
        StreamerView dataView = createDataView(location);
        dataView.initComponents();
        StreamerPanel panel = new StreamerPanel(dataView);
        panel.initComponents();
        panelsMap.put(location, panel);
        return panel;
    }

    public Collection<StreamerPanel> getPanels() {
        return panelsMap.values();
    }

    public StreamerPanel getPanel(PanelLocation location) {
        return panelsMap.get(location);
    }

    public StreamerView getOppositeView() {
        return panelsMap.keySet()
            .stream()
            .filter(location -> location != currentLocation)
            .findFirst()
            .map(panelsMap::get)
            .map(StreamerPanel::getView)
            .orElseThrow(() -> new RuntimeException("Cannot find the opposite view"));
    }
}
