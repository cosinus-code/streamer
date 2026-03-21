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

package org.cosinus.streamer.pack.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class ArchiveStreamEntry implements Comparable<ArchiveStreamEntry> {

    private final Path path;

    private final ArchiveEntry archiveEntry;

    private final Supplier<InputStream> inputStreamSupplier;

    public ArchiveStreamEntry(ArchiveEntry archiveEntry) {
        this(archiveEntry, null);
    }

    public ArchiveStreamEntry(final ArchiveEntry archiveEntry,
                              final Supplier<InputStream> inputStreamSupplier) {
        this.archiveEntry = archiveEntry;
        this.inputStreamSupplier = inputStreamSupplier;
        this.path = Paths.get(archiveEntry.getName());
    }

    public ArchiveEntry getArchiveEntry() {
        return archiveEntry;
    }

    public long getSize() {
        return isDirectory() ? -1L : archiveEntry.getSize();
    }

    public long lastModified() {
        return ofNullable(archiveEntry.getLastModifiedDate())
            .map(Date::getTime)
            .orElse(0L);
    }

    public boolean isDirectory() {
        return archiveEntry.isDirectory();
    }

    public Path toPath() {
        return path;
    }

    public Path getPath() {
        return path;
    }

    public Optional<Path> getParentPath() {
        return ofNullable(path.getParent());
    }

    public String getName() {
        return archiveEntry.getName();
    }

    public InputStream getEntryInputStream() {
        return ofNullable(inputStreamSupplier)
            .map(Supplier::get)
            .orElse(null);
    }

    public boolean isOSSpecific() {
        return archiveEntry.getName().startsWith("__MACOSX") ||
            archiveEntry.getName().startsWith("__MACOSX64") ||
            archiveEntry.getName().equals(".DS_Store");
    }

    @Override
    public int compareTo(ArchiveStreamEntry entry) {
        return getName().compareTo(entry.getName());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArchiveStreamEntry entry)) {
            return false;
        }

        return getName().equals(entry.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
