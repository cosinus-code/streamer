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

package org.cosinus.streamer.zip;

import org.cosinus.streamer.zip.stream.ZipStreamEntry;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

/**
 * Zip content holder
 */
public class ZipHolder {

    private static final String UNIX_SEPARATOR = "/";

    private static final String ZIP_ROOT = UNIX_SEPARATOR;

    private final Map<String, ZipStreamEntry> entriesMap;

    private final Map<String, Set<ZipStreamEntry>> entriesGroupedByParentMap;

    private boolean loaded;

    public ZipHolder() {
        this.entriesMap = new HashMap<>();
        this.entriesGroupedByParentMap = new HashMap<>();
    }

    public void add(ZipStreamEntry zipEntry) {
        entriesMap.put(key(zipEntry.toPath()), zipEntry);

        String parentPath = zipEntry.getParentPath()
                .map(this::key)
                .orElse(ZIP_ROOT);
        entriesGroupedByParentMap
                .computeIfAbsent(parentPath, key -> new HashSet<>())
                .add(zipEntry);
    }

    private String key(Path path) {
        return key(path.toString());
    }

    private String key(String path) {
        return separatorsToUnix(path);
    }

    public Stream<ZipStreamEntry> listEntries(Path path) {
        return listEntries(key(path));
    }

    public Stream<ZipStreamEntry> listEntries(String path) {
        return Optional.ofNullable(entriesGroupedByParentMap.get(key(path)))
                .stream()
                .flatMap(Collection::stream);
    }

    public Stream<ZipStreamEntry> rootEntries() {
        return listEntries(ZIP_ROOT);
    }

    public Optional<ZipStreamEntry> get(Path path) {
        return Optional.ofNullable(path)
                .map(this::key)
                .map(entriesMap::get);
    }

    public Optional<ZipStreamEntry> get(String path) {
        return Optional.ofNullable(entriesMap.get(key(path)));
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
