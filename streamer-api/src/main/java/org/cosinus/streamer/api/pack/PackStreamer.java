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

import org.cosinus.streamer.api.ContainerStreamer;
import org.cosinus.streamer.api.TransferStreamer;
import org.cosinus.streamer.api.Streamer;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Pack streamer interface
 */
public abstract class PackStreamer<T> implements Streamer<T> {

    protected final TransferStreamer transferStreamer;

    public PackStreamer(TransferStreamer transferStreamer) {
        this.transferStreamer = transferStreamer;
    }

    @Override
    public Streamer<T> create() {
        return transferStreamer.create();
    }

    @Override
    public ContainerStreamer getParent() {
        return transferStreamer.getParent();
    }

    @Override
    public boolean delete() {
        return transferStreamer.delete();
    }

    @Override
    public String getProtocol() {
        return transferStreamer.getProtocol();
    }

    @Override
    public Path getPath() {
        return transferStreamer.getPath();
    }

    @Override
    public boolean exists() {
        return transferStreamer.exists();
    }

    @Override
    public long getSize() {
        return transferStreamer.getSize();
    }

    @Override
    public long lastModified() {
        return transferStreamer.lastModified();
    }

    @Override
    public long getFreeSpace() {
        return transferStreamer.getParent().getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return transferStreamer.getParent().getTotalSpace();
    }

    @Override
    public String getUrlPath() {
        return transferStreamer.getUrlPath();
    }

    public abstract Optional<T> find(String path);

    public abstract void finishLoading();
}
