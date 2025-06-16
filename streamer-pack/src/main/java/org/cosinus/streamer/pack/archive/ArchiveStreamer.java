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
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.*;
import org.cosinus.streamer.api.worker.SaveWorkerModel;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;

public abstract class ArchiveStreamer<T> implements Streamer<T> {

    protected final ArchivePackStreamer<? extends ArchiveStreamer> archivePackStreamer;

    protected final ArchiveStreamEntry archiveEntry;

    protected final List<TranslatableName> detailNames;

    protected final List<Value> details;

    public ArchiveStreamer(ArchivePackStreamer<? extends ArchiveStreamer> archivePackStreamer,
                           ArchiveStreamEntry archiveEntry) {
        this.archivePackStreamer = archivePackStreamer;
        this.archiveEntry = archiveEntry;
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_TYPE, null),
            new TranslatableName(DETAIL_KEY_SIZE, null),
            new TranslatableName(DETAIL_KEY_TIME, null));
        this.details = asList(
            new TextValue(getName()),
            new TextValue(getType()),
            new MemoryValue(getSize()),
            new DateValue(lastModified()));

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

    public ArchiveEntry getArchiveEntry() {
        return archiveEntry.getArchiveEntry();
    }

    public InputStream getArchiveInputStream() {
        return archiveEntry.getEntryInputStream();
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

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean delete() {
        return archivePackStreamer.delete(archiveEntry);
    }

    @Override
    public boolean isDirty() {
        return archivePackStreamer.isDirty();
    }

    @Override
    public SaveWorkerModel<?> saveModel() {
        return archivePackStreamer.saveModel();
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public List<Value> details() {
        return details;
    }
}
