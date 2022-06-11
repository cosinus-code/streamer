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

package org.cosinus.streamer.api.meta;

import org.cosinus.streamer.api.ContainerStreamer;
import org.cosinus.streamer.api.Streamer;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class MainStreamer<S extends Streamer> implements ContainerStreamer<S>, StreamerFinder {

    private ContainerStreamer parent;

    private Path path;

    public void setName(String name) {
        this.path = Paths.get(name);
    }

    public void setParent(ContainerStreamer parent) {
        this.parent = parent;
    }

    @Override
    public ContainerStreamer getParent() {
        return parent;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getProtocol() {
        return MetaStreamer.META_PROTOCOL;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public long getFreeSpace() {
        return 0;
    }

    @Override
    public long getTotalSpace() {
        return 0;
    }

    @Override
    public MainStreamer create() {
        return this;
    }

    @Override
    public boolean delete() {
        return false;
    }
}
