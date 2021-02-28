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
import org.cosinus.streamer.api.pack.PackStreamer;
import org.cosinus.streamer.zip.stream.ZipEntryOutputStream;
import org.cosinus.streamer.zip.stream.ZipStream;
import org.cosinus.streamer.zip.stream.ZipStreamEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

public class ZipPackStreamer implements DirectoryStreamer<ZipStreamer>, PackStreamer<ZipStreamer> {

    private ZipHolder zipHolder;

    private final InputStreamer packInputStreamer;

    private ZipOutputStream outputStream;

    protected ZipPackStreamer(InputStreamer packInputStreamer) {
        this.packInputStreamer = packInputStreamer;
    }

    public InputStreamer getPackInputStreamer() {
        return packInputStreamer;
    }

    public ZipHolder getZipHolder() {
        if (zipHolder == null || !zipHolder.isLoaded()) {
            zipHolder = new ZipHolder();
            ZipStream.of(packInputStreamer.inputStream())
                    .forEach(zipHolder::add);
            zipHolder.setLoaded(true);
        }
        return zipHolder;
    }

    @Override
    public Stream<? extends ZipStreamer> stream() {
        if (zipHolder == null || !zipHolder.isLoaded()) {
            zipHolder = new ZipHolder();
            return ZipStream.of(packInputStreamer.inputStream())
                    .peek(zipHolder::add)
                    .filter(zipEntry -> zipEntry.getParentPath().isEmpty())
                    .map(this::createZipStreamer);
        }

        return zipHolder.rootEntries()
                .map(this::createZipStreamer);
    }

    @Override
    public Stream<ZipStreamer> flatStream(StreamerFilter streamerFilter) {
        return ZipStream.walk(packInputStreamer.inputStream(), "")
                .map(this::createZipStreamer);
    }

    @Override
    public void finishLoading() {
        if (zipHolder != null) {
            zipHolder.setLoaded(true);
        }
    }

    @Override
    public Optional<ZipStreamer> find(String path) {
        return getZipHolder().get(path)
                .map(this::createZipStreamer);
    }

    @Override
    public long getFreeSpace() {
        return packInputStreamer.getParent().getFreeSpace();
    }

    @Override
    public ZipDirectoryStreamer createDirectoryStreamer(Path path) {
        ZipStreamEntry zipEntry = zipHolder.get(path)
                .orElse(new ZipStreamEntry(path.toString() + "/"));
        return createDirectoryStreamer(zipEntry);
    }

    @Override
    public ZipBinaryStreamer createBinaryStreamer(Path path) {
        ZipStreamEntry zipEntry = zipHolder.get(path)
                .orElse(new ZipStreamEntry(path.toString()));
        return createBinaryStreamer(zipEntry);
    }

    @Override
    public boolean rename(Path path, String newName) {
        return false;
    }

    @Override
    public void execute(Path path) {

    }

    @Override
    public StreamConsumer<? extends ZipStreamer> saver(boolean append) {
        return packInputStreamer.saver(append);
    }

    @Override
    public Streamer save() {
        return packInputStreamer.save();
    }

    @Override
    public DirectoryStreamer getParent() {
        return packInputStreamer.getParent();
    }

    @Override
    public boolean delete() {
        return packInputStreamer.delete();
    }

    @Override
    public String getProtocol() {
        return packInputStreamer.getProtocol();
    }

    @Override
    public Path getPath() {
        return packInputStreamer.getPath();
    }

    @Override
    public boolean exists() {
        return packInputStreamer.exists();
    }

    @Override
    public long getSize() {
        return packInputStreamer.getSize();
    }

    @Override
    public long lastModified() {
        return packInputStreamer.lastModified();
    }

    public ZipStreamer createZipStreamer(ZipStreamEntry zipEntry) {
        return zipEntry.isDirectory() ?
                createDirectoryStreamer(zipEntry) :
                createBinaryStreamer(zipEntry);
    }

    public ZipDirectoryStreamer createDirectoryStreamer(ZipStreamEntry zipEntry) {
        return new ZipDirectoryStreamer(this, zipEntry);
    }

    public ZipBinaryStreamer createBinaryStreamer(ZipStreamEntry zipEntry) {
        return new ZipBinaryStreamer(this, zipEntry);
    }

    public OutputStream getEntryOutputStream(ZipStreamEntry zipEntry) throws IOException {
        return new ZipEntryOutputStream(outputStream, zipEntry);
    }

    public ZipOutputStream getOutputStream(boolean append) {
        if (outputStream == null) {
            outputStream = createOutputStream(append);
        }
        return outputStream;
    }

    public ZipOutputStream createOutputStream(boolean append) {
        return new ZipOutputStream(packInputStreamer.outputStream(append));
    }

    public boolean exists(Path path) {
        return zipHolder.get(path).isPresent();
    }

    Stream<ZipStreamEntry> listEntries(Path path) {
        return zipHolder.listEntries(path);
    }
}
