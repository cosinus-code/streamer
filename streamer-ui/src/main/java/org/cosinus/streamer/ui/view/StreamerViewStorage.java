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

package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.store.ApplicationStorage;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Storage functionality for streamer views
 */
@Component
public class StreamerViewStorage {

    public static final String LAST_STREAMER = "last.streamer.";

    public static final String PREFERRED_VIEW = "preferred.view.";

    private final ApplicationStorage applicationStorage;

    public StreamerViewStorage(ApplicationStorage applicationStorage) {
        this.applicationStorage = applicationStorage;
    }

    public Optional<String> loadLastLoadedStreamer(PanelLocation location) {
        return ofNullable(applicationStorage.getString(LAST_STREAMER + location));
    }

    public <T> void saveLastLoadedStreamer(Streamer<T> streamer, PanelLocation location) {
        ofNullable(streamer)
            .map(Streamer::getUrlPath)
            .ifPresent(path -> applicationStorage.saveString(LAST_STREAMER + location, path));
    }

    /**
     * @param streamer the streamer to show
     * @param location the location of the panel to show it in
     * @return the name of the view preferred in the panel for the kind of views of the streamer
     * (see {@link Streamer#getViewId()})
     */
    public <T> Optional<String> loadPreferredView(Streamer<T> streamer, PanelLocation location) {
        return ofNullable(streamer)
            .map(Streamer::getViewId)
            .map(viewId -> applicationStorage.getString(preferredViewKey(viewId, location)));
    }

    /**
     * Save the view as preferred in the panel for the kind of views of the streamer
     * (see {@link Streamer#getViewId()}).
     *
     * @param streamer the streamer shown
     * @param location the location of the panel showing it
     * @param viewName the name of the view
     */
    public <T> void savePreferredView(Streamer<T> streamer, PanelLocation location, String viewName) {
        ofNullable(streamer)
            .map(Streamer::getViewId)
            .ifPresent(viewId -> applicationStorage.saveString(preferredViewKey(viewId, location), viewName));
    }

    private String preferredViewKey(String viewId, PanelLocation location) {
        return PREFERRED_VIEW + location + "." + viewId;
    }
}
