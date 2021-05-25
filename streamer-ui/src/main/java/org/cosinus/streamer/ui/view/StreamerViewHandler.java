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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.view.table.details.DetailViewCreator;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.Preferences;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.LEFT_VIEW;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.RIGHT_VIEW;
import static org.cosinus.streamer.ui.view.PanelLocation.LEFT;

/**
 * Handler for data views
 */
@Component
public class StreamerViewHandler {

    private PanelLocation currentLocation = LEFT;

    private final Map<PanelLocation, StreamerPanel> panelsMap = new HashMap<>();

    private final Preferences preferences;

    private final StreamerHandler streamerHandler;

    private final Map<Optional<String>, List<StreamerViewCreator>> streamerViewCreatorsMap;

    private final StreamerViewCreator defaultStreamerViewCreator;

    public StreamerViewHandler(Preferences preferences,
                               StreamerHandler streamerHandler,
                               Set<StreamerViewCreator> streamerViewCreators,
                               DetailViewCreator defaultStreamerViewCreator) {
        this.preferences = preferences;
        this.streamerHandler = streamerHandler;
        this.streamerViewCreatorsMap = streamerViewCreators
            .stream()
            .collect(groupingBy(streamerViewCreator -> ofNullable(streamerViewCreator.getHandledType())));
        this.defaultStreamerViewCreator = defaultStreamerViewCreator;
    }

    public PanelLocation getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(PanelLocation currentLocation) {
        this.currentLocation = currentLocation;
        setActiveView(currentLocation);
    }

    public StreamerView getCurrentView() {
        return getPanel(currentLocation)
            .map(StreamerPanel::getView)
            .orElse(null);
    }

    private void setActiveView(PanelLocation currentLocation) {
        panelsMap.forEach((location, panel) -> ofNullable(panel.getView())
            .ifPresent(view -> view.setActive(location == currentLocation)));
    }

    public StreamerView createStreamerView(Streamer streamer, PanelLocation location) {
        StreamerView view = getStreamerViewCreator(streamer.getType(), location)
            .createStreamerView(location);
        view.initComponents();
        getPanel(location)
            .ifPresent(panel -> panel.setView(view));
        return view;
    }

    public StreamerViewCreator getStreamerViewCreator(String type, PanelLocation location) {
        return ofNullable(streamerViewCreatorsMap.get(ofNullable(type)))
            .stream()
            .flatMap(Collection::stream)
            .filter(streamerViewCreator -> isPreferredView(streamerViewCreator, location))
            .findFirst()
            .orElse(defaultStreamerViewCreator);
    }

    protected boolean isPreferredView(StreamerViewCreator streamerViewCreator, PanelLocation location) {
        return streamerViewCreator.getHandledType() == null ?
            preferences.findPreference(LEFT == location ? LEFT_VIEW : RIGHT_VIEW)
                .map(Preference::getRealValue)
                .map(streamerViewCreator.getName()::equals)
                .orElse(false) :
            false;
    }

    public StreamerPanel createStreamerPanel(PanelLocation location) {
        StreamerPanel panel = new StreamerPanel();
        panelsMap.put(location, panel);
        return panel;
    }

    public Collection<StreamerPanel> getPanels() {
        return panelsMap.values();
    }

    public Optional<StreamerPanel> getPanel(PanelLocation location) {
        return ofNullable(panelsMap.get(location));
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
