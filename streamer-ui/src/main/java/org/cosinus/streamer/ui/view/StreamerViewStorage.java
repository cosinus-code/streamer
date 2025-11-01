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

//    public static final String LAST_VIEW = "last.view.";
//
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
//
//    public Optional<String> loadLastLoadedView(PanelLocation location) {
//        return ofNullable(applicationStorage.getString(LAST_VIEW + location));
//    }
//
//    public <T> void saveLastLoadedView(StreamerView<T, T> streamerView, PanelLocation location) {
//        ofNullable(streamerView)
//            .map(StreamerView::getName)
//            .ifPresent(path -> applicationStorage.saveString(LAST_VIEW + location, path));
//    }
}
