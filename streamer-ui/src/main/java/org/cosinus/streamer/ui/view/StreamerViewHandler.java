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

import org.cosinus.streamer.ui.view.table.details.DetailViewCreator;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.Preferences;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.*;
import static org.cosinus.streamer.ui.view.PanelLocation.LEFT;
import static org.cosinus.streamer.ui.view.PanelLocation.RIGHT;

/**
 * Handler for data views
 */
@Component
public class StreamerViewHandler {

    private PanelLocation currentLocation;

    private final Map<PanelLocation, StreamerPanel> panelsMap = new HashMap<>();

    private final Preferences preferences;

    private final Map<String, StreamerViewCreator> streamerViewCreatorsMap;

    private final StreamerViewCreator defaultStreamerViewCreator;

    private String preferredViewName;

    public StreamerViewHandler(Preferences preferences,
                               Set<StreamerViewCreator> streamerViewCreators,
                               DetailViewCreator defaultStreamerViewCreator) {
        this.preferences = preferences;
        this.streamerViewCreatorsMap = streamerViewCreators
            .stream()
            .collect(toMap(StreamerViewCreator::getViewName, identity()));
        this.defaultStreamerViewCreator = defaultStreamerViewCreator;
        this.currentLocation = preferences.booleanPreference(SHOW_LEFT_VIEW) ? LEFT : RIGHT;
    }

    public PanelLocation getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(PanelLocation currentLocation) {
        this.currentLocation = currentLocation;
        setActiveView(currentLocation);
    }

    public StreamerView<?, ?> getCurrentView() {
        return getPanel(currentLocation)
            .map(StreamerPanel::getView)
            .orElse(null);
    }

    private void setActiveView(PanelLocation currentLocation) {
        panelsMap.forEach((location, panel) -> ofNullable(panel.getView())
            .ifPresent(view -> view.setActive(location == currentLocation)));
    }

    public <T, V> StreamerView<T, V> getStreamerView(PanelLocation location, String streamerViewName) {
        Optional<String> resolvedViewName = ofNullable(streamerViewName)
            .or(() -> getPreferredViewName(location));

        StreamerView<T, V> view = resolvedViewName
            .map(streamerViewCreatorsMap::get)
            //TODO: to avoid cast
            .map(streamerViewCreator -> (StreamerView<T, V>) streamerViewCreator.createStreamerView(location))
            .orElseGet(() -> (StreamerView<T, V>) defaultStreamerViewCreator.createStreamerView(location));

        getPanel(location)
            .ifPresent(panel -> panel.setView(view));

        return view;
    }

    protected Optional<String> getPreferredViewName(PanelLocation location) {
        return ofNullable(preferredViewName)
            .or(() -> preferences.findPreference(LEFT == location ? LEFT_VIEW : RIGHT_VIEW)
                .map(Preference::getRealValue)
                .map(Object::toString));
    }

    public void setPreferredViewName(String preferredViewName) {
        this.preferredViewName = preferredViewName;
    }

    public StreamerPanel createStreamerPanel(PanelLocation location) {
        StreamerPanel panel = new StreamerPanel();
        panelsMap.put(location, panel);
        panel.initComponents();
        panel.setView(getStreamerView(location, null));
        return panel;
    }

    public Collection<StreamerPanel> getPanels() {
        return panelsMap.values();
    }

    public Optional<StreamerView> getView(PanelLocation location) {
        return getPanel(location)
            .map(StreamerPanel::getView);
    }

    public Optional<StreamerPanel> getPanel(PanelLocation location) {
        return ofNullable(panelsMap.get(location));
    }

    public void reloadViews() {
        stream(PanelLocation.values())
            .map(this::getView)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(StreamerView::reload);
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
