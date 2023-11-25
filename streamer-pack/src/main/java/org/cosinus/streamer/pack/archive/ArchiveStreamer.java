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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

import java.nio.file.Path;
import java.util.Objects;

public abstract class ArchiveStreamer<T> implements Streamer<T> {

    protected final ArchivePackStreamer<? extends ArchiveStreamer> archivePackStreamer;

    protected final ArchiveStreamEntry archiveEntry;

    public ArchiveStreamer(ArchivePackStreamer<? extends ArchiveStreamer> archivePackStreamer,
                           ArchiveStreamEntry archiveEntry) {
        this.archivePackStreamer = archivePackStreamer;
        this.archiveEntry = archiveEntry;
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return archivePackStreamer.createBinaryStreamer(path);
    }


    @Override
    public ParentStreamer<?> getParent() {
        return archiveEntry.getParentPath()
            .flatMap(archivePackStreamer::findDirectoryStreamer)
            .orElse(archivePackStreamer);
    }

    @Override
    public Path getPath() {
        return archiveEntry.getPath();
    }

    @Override
    public String getUrlPath() {
        return archivePackStreamer.getUrlPath() + "#" + getPath();
    }

    @Override
    public boolean exists() {
        return archivePackStreamer.exists(getPath());
    }

    @Override
    public long getSize() {
        return archiveEntry.getSize();
    }

    @Override
    public long lastModified() {
        return archiveEntry.lastModified();
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArchiveStreamer)) {
            return false;
        }

        ArchiveStreamer that = (ArchiveStreamer) other;
        return archiveEntry.equals(that.archiveEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archiveEntry);
    }
}
