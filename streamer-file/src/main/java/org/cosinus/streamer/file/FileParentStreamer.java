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

package org.cosinus.streamer.file;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.StreamerSizeHandler;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.streamer.api.value.MemoryValue;
import org.cosinus.streamer.api.value.Value;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class FileParentStreamer extends FileStreamer<FileStreamer<?>> implements ParentStreamer<FileStreamer<?>> {

    @Autowired
    private StreamerSizeHandler streamerSizeHandler;

    public FileParentStreamer(Path path) {
        super(path);
    }

    @Override
    public Stream<FileStreamer<?>> stream() {
        return ofNullable(getPath())
            .map(fileHandler::list)
            .orElseGet(Stream::empty)
            .map(fileMainStreamer::create);
    }

    @Override
    public Stream<FileStreamer<?>> flatStream(StreamerFilter streamerFilter) {
        return stream()
                .filter(streamerFilter)
                .map(Streamer::getPath)
                .flatMap(fileHandler::walk)
                .map(fileMainStreamer::create);
    }

    @Override
    public void execute(Path path) {
        fileMainStreamer.execute(path);
    }

    @Override
    public FileStreamer<?> create(Path path, boolean parent) {
        return fileMainStreamer.create(path, parent);
    }

    @Override
    public long getFreeSpace() {
        return file.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return file.getTotalSpace();
    }

    @Override
    public void createAndSave() {
        if (!file.exists() && !file.mkdirs()) {
            throw new SaveStreamerException("Failed to create directory:" + file.getPath());
        }
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public long getSize() {
        return streamerSizeHandler.getSize(this);
    }

    @Override
    protected boolean isSizeComputing() {
        return streamerSizeHandler.isStreamerSizeComputing(this);
    }

    @Override
    public List<Value> details() {
        List<Value> details = super.details();
        details.set(DETAIL_INDEX_SIZE, new MemoryValue(getSize(), isSizeComputing()));
        return details;
    }
}
