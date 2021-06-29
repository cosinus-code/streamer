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

package org.cosinus.streamer.api.pack;

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.InputStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.consumer.StreamConsumer;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Pack streamer interface
 */
public abstract class PackStreamer<T> implements Streamer<T> {

    protected final InputStreamer packInputStreamer;

    public PackStreamer(InputStreamer packInputStreamer) {
        this.packInputStreamer = packInputStreamer;
    }

    @Override
    public StreamConsumer<? extends T> saver(boolean append) {
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
    public DirectoryStreamer getRootStreamer() {
        return packInputStreamer.getRootStreamer();
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

    @Override
    public long getFreeSpace() {
        return packInputStreamer.getParent().getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return packInputStreamer.getParent().getTotalSpace();
    }

    @Override
    public String getUrlPath() {
        return packInputStreamer.getUrlPath();
    }

    public abstract Optional<T> find(String path);

    public abstract void finishLoading();
}
