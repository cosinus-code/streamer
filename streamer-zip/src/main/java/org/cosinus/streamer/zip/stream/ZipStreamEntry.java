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

package org.cosinus.streamer.zip.stream;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;

/**
 * Wrapper over {@link ZipEntry} used in {@link ZipStream}
 */
public class ZipStreamEntry extends ZipEntry implements Comparable<ZipStreamEntry> {

    private final Path path;

    private final ZipEntryInputStream zipEntryInputStream;

    public ZipStreamEntry(String name) {
        this(name, null);
    }

    public ZipStreamEntry(String name,
                          ZipEntryInputStream zipEntryInputStream) {
        super(name);
        this.zipEntryInputStream = zipEntryInputStream;
        path = Paths.get(getName());
    }

    public ZipStreamEntry(ZipEntry e,
                          ZipEntryInputStream zipEntryInputStream) {
        super(e);
        this.zipEntryInputStream = zipEntryInputStream;
        path = Paths.get(getName());
    }

    public Path toPath() {
        return path;
    }

    public Path getPath() {
        return path;
    }

    public Optional<Path> getParentPath() {
        return Optional.ofNullable(path.getParent());
    }

    @Override
    public int compareTo(@NotNull ZipStreamEntry zipEntry) {
        return getName().compareTo(zipEntry.getName());
    }

    public ZipEntryInputStream getZipInputStream() {
        return zipEntryInputStream;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ZipStreamEntry)) {
            return false;
        }

        ZipEntry zipEntry = (ZipEntry) other;
        return getName().equals(zipEntry.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
