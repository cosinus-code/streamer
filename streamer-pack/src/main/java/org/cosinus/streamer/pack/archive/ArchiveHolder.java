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

package org.cosinus.streamer.pack.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.pack.archive.stream.ArchiveCache;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Archive content holder
 */
public class ArchiveHolder implements ArchiveCache {

    private static final String UNIX_SEPARATOR = "/";

    private static final String ARCHIVE_ROOT = UNIX_SEPARATOR;

    private final Map<String, ArchiveStreamEntry> entriesMap;

    private final Map<String, Set<ArchiveStreamEntry>> entriesGroupedByParentMap;

    private boolean loaded;

    private boolean dirty;

    public ArchiveHolder() {
        injectContext(this);
        this.entriesMap = new TreeMap<>();
        this.entriesGroupedByParentMap = new TreeMap<>();
    }

    @Override
    public void add(ArchiveStreamEntry archiveEntry) {
        entriesMap.put(key(archiveEntry.toPath()), archiveEntry);

        String parentPath = archiveEntry.getParentPath().map(this::key).orElse(ARCHIVE_ROOT);

        entriesGroupedByParentMap.computeIfAbsent(parentPath, key -> new TreeSet<>()).add(archiveEntry);

        archiveEntry.getParentPath().ifPresent(this::checkPath);
    }

    private void checkPath(Path path) {
        String entryKey = key(path.toString());
        if (!entriesMap.containsKey(entryKey)) {
            ArchiveStreamEntry missingEntry = new ArchiveStreamEntry(new VirtualDirectoryArchiveEntry(path.toString()));
            entriesMap.put(entryKey, missingEntry);

            Path parentPath = path.getParent();
            if (parentPath != null && !parentPath.toString().equals(ARCHIVE_ROOT)) {
                String parentKey = key(parentPath.toString());
                entriesGroupedByParentMap.computeIfAbsent(parentKey, key -> new TreeSet<>()).add(missingEntry);
                checkPath(parentPath);
            }
        }
    }

    public String key(Path path) {
        return key(path.toString());
    }

    public String key(String path) {
        return separatorsToUnix(path);
    }

    public Stream<ArchiveStreamEntry> listEntries(Path path) {
        return listEntries(key(path));
    }

    public Stream<ArchiveStreamEntry> listEntries(String path) {
        return ofNullable(entriesGroupedByParentMap.get(key(path)))
            .stream()
            .flatMap(Collection::stream);
    }

    public Stream<ArchiveStreamEntry> listEntries() {
        return entriesMap.values().stream();
    }

    public Stream<ArchiveStreamEntry> rootEntries() {
        return listEntries(ARCHIVE_ROOT);
    }

    public Optional<ArchiveStreamEntry> get(Path path) {
        return ofNullable(path).map(this::key).map(entriesMap::get);
    }

    public Optional<ArchiveStreamEntry> get(String path) {
        return ofNullable(entriesMap.get(key(path)));
    }

    @Override
    public boolean contains(ArchiveEntry archiveEntry) {
        return entriesMap.containsKey(key(Paths.get(archiveEntry.getName())));
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void evict() {
        if (!loaded) {
            throw new StreamerException("Cannot reset still loading archive stremer");
        }
        entriesMap.clear();
        entriesGroupedByParentMap.clear();
        loaded = false;
    }

    @Override
    public boolean evict(ArchiveStreamEntry archiveEntry) {
        String entryPathToDelete = key(archiveEntry.getPath());
        dirty = entriesMap.remove(entryPathToDelete) != null;

        if (dirty) {
            if (archiveEntry.isDirectory()) {
                entriesMap.keySet()
                    .removeIf(entryPath -> entryPath.startsWith(entryPathToDelete));
                entriesGroupedByParentMap.keySet()
                    .removeIf(parentEntryPath -> parentEntryPath.startsWith(entryPathToDelete));
            }

            archiveEntry.getParentPath()
                .map(this::key)
                .map(entriesGroupedByParentMap::get)
                .ifPresent(childEntries -> childEntries
                    .removeIf(childEntry -> key(childEntry.getPath()).equals(entryPathToDelete)));
        }

        return dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
