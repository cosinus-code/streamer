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

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.InputStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.consumer.StreamConsumer;
import org.cosinus.streamer.zip.stream.ZipStream;
import org.cosinus.streamer.zip.stream.ZipStreamEntry;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class ZipDirectoryStreamer extends ZipStreamer<ZipStreamer> implements DirectoryStreamer<ZipStreamer> {

    protected ZipDirectoryStreamer(ZipPackStreamer zipPackStreamer, ZipStreamEntry zipEntry) {
        super(zipPackStreamer, zipEntry);
    }

    @Override
    public Stream<ZipStreamer> stream() {
        return zipPackStreamer.listEntries(getPath())
                .map(zipPackStreamer::createZipStreamer);
    }

    @Override
    public Stream<ZipStreamer> flatStream(StreamerFilter streamerFilter) {
        InputStreamer packInputStreamer = zipPackStreamer.getPackInputStreamer();
        return ZipStream.walk(packInputStreamer.inputStream(), zipEntry.getName())
                .map(zipPackStreamer::createZipStreamer)
                .filter(streamerFilter);
    }

    @Override
    public StreamConsumer<? extends ZipStreamer> saver(boolean append) {
        return null;
    }

    @Override
    public long getFreeSpace() {
        return Optional.of(zipPackStreamer)
                .map(ZipPackStreamer::getPackInputStreamer)
                .map(Streamer::getParent)
                .map(DirectoryStreamer::getFreeSpace)
                .orElse(0L);
    }

    @Override
    public ZipDirectoryStreamer createDirectoryStreamer(Path path) {
        return zipPackStreamer.createDirectoryStreamer(path);
    }

    @Override
    public ZipBinaryStreamer createBinaryStreamer(Path path) {
        return zipPackStreamer.createBinaryStreamer(path);
    }

    @Override
    public boolean rename(Path path, String newName) {
        return false;
    }

    @Override
    public void execute(Path path) {

    }
}
