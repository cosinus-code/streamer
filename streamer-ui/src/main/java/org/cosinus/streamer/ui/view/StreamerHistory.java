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

import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.store.ApplicationStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.HISTORY_SIZE;

/**
 * History of the streamer paths shown in each panel location, kept in the application storage.
 * <p>
 * For each location the storage holds the number of paths, the current position and one key per path
 * (a single value holding all the paths could exceed the storage value size limit).
 * <p>
 * Going back or forward only moves the current position.
 * Adding a path drops all the paths after the current position, then appends the new path,
 * which becomes the current one. Adding the path already at the current position does nothing,
 * so reloading a streamer, or showing the one reached by going back, does not change the history.
 */
@Component
public class StreamerHistory {

    public static final int DEFAULT_HISTORY_SIZE = 200;

    private static final String HISTORY = "history";

    private static final String SIZE = "size";

    private static final String POSITION = "position";

    private final ApplicationStorage applicationStorage;

    private final IntSupplier maxSizeSupplier;

    @Autowired
    public StreamerHistory(final ApplicationStorage applicationStorage,
                           final Preferences preferences) {
        this(applicationStorage, () -> preferences.findIntPreference(HISTORY_SIZE).orElse(DEFAULT_HISTORY_SIZE));
    }

    public StreamerHistory(final ApplicationStorage applicationStorage,
                           final IntSupplier maxSizeSupplier) {
        this.applicationStorage = applicationStorage;
        this.maxSizeSupplier = maxSizeSupplier;
    }

    /**
     * Add a path to the history of a location, dropping the paths after the current position.
     *
     * @param location the panel location
     * @param path     the url path of the streamer shown
     */
    public synchronized void add(final PanelLocation location, final String path) {
        if (location == null || path == null || path.isBlank()) {
            return;
        }

        List<String> paths = loadPaths(location);
        int position = getPosition(location, paths.size());
        if (position >= 0 && path.equals(paths.get(position))) {
            return;
        }

        List<String> newPaths = new ArrayList<>(paths.subList(0, position + 1));
        newPaths.add(path);

        int maxSize = Math.max(1, maxSizeSupplier.getAsInt());
        if (newPaths.size() > maxSize) {
            newPaths = new ArrayList<>(newPaths.subList(newPaths.size() - maxSize, newPaths.size()));
        }

        savePaths(location, newPaths, paths.size());
        applicationStorage.saveInt(key(location, POSITION), newPaths.size() - 1);
    }

    /**
     * Move back in the history of a location.
     *
     * @param location the panel location
     * @return the previous path, which becomes the current one, or empty if there is no previous path
     */
    public synchronized Optional<String> back(final PanelLocation location) {
        return move(location, -1);
    }

    /**
     * Move forward in the history of a location.
     *
     * @param location the panel location
     * @return the next path, which becomes the current one, or empty if there is no next path
     */
    public synchronized Optional<String> forward(final PanelLocation location) {
        return move(location, 1);
    }

    private Optional<String> move(final PanelLocation location, final int step) {
        if (location == null) {
            return empty();
        }

        int size = getSize(location);
        int newPosition = getPosition(location, size) + step;
        if (newPosition < 0 || newPosition >= size) {
            return empty();
        }

        Optional<String> path = ofNullable(applicationStorage.getString(key(location, newPosition)));
        path.ifPresent(found -> applicationStorage.saveInt(key(location, POSITION), newPosition));
        return path;
    }

    /**
     * @param location the panel location
     * @return the paths in the history of the location, the oldest first
     */
    public synchronized List<String> getPaths(final PanelLocation location) {
        return loadPaths(location);
    }

    /**
     * @param location the panel location
     * @return the current position in the history of the location, or -1 if the history is empty
     */
    public synchronized int getPosition(final PanelLocation location) {
        return getPosition(location, getSize(location));
    }

    private List<String> loadPaths(final PanelLocation location) {
        int size = getSize(location);
        List<String> paths = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            paths.add(applicationStorage.getString(key(location, index)));
        }
        return paths.stream()
            .filter(Objects::nonNull)
            .toList();
    }

    private void savePaths(final PanelLocation location, final List<String> paths, final int oldSize) {
        for (int index = 0; index < paths.size(); index++) {
            applicationStorage.saveString(key(location, index), paths.get(index));
        }
        for (int index = paths.size(); index < oldSize; index++) {
            applicationStorage.remove(key(location, index));
        }
        applicationStorage.saveInt(key(location, SIZE), paths.size());
    }

    private int getSize(final PanelLocation location) {
        return Math.max(0, applicationStorage.getInt(key(location, SIZE), 0));
    }

    private int getPosition(final PanelLocation location, final int size) {
        int position = applicationStorage.getInt(key(location, POSITION), size - 1);
        return Math.min(Math.max(position, -1), size - 1);
    }

    private String key(final PanelLocation location, final int index) {
        return key(location, Integer.toString(index));
    }

    private String key(final PanelLocation location, final String name) {
        return applicationStorage.key(HISTORY, location.toString(), name);
    }
}
