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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;

import java.nio.file.Path;
import java.util.stream.Stream;

public class ArchiveParentStreamer extends ArchiveStreamer<ArchiveStreamer> implements ParentStreamer<ArchiveStreamer> {

    public ArchiveParentStreamer(final ArchivePackStreamer archivePackStreamer,
                                 final ArchiveStreamEntry archiveEntry) {
        super(archivePackStreamer, archiveEntry);
    }

    @Override
    public Stream<ArchiveStreamer> stream() {
        return archivePackStreamer.listEntries(getPath())
            .map(entry -> archivePackStreamer.createArchiveStreamer(entry));
    }

    @Override
    public Stream flatStream(StreamerFilter streamerFilter) {
        return archivePackStreamer.flatStream(streamerFilter, getPath());
    }

    @Override
    public long getFreeSpace() {
        return archivePackStreamer.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return archivePackStreamer.getTotalSpace();
    }

    @Override
    public void execute(Path path) {

    }

    @Override
    public ArchiveStreamer create(Path path, boolean parent) {
        return archivePackStreamer.create(path, parent);
    }

    @Override
    public ArchiveStreamer create(Path path, Streamer<?> source) {
        return archivePackStreamer.create(path, source);
    }
}
