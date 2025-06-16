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

package org.cosinus.streamer.api.expand;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Pack streamer interface
 */
public abstract class ExpandedStreamer<T> implements Streamer<T> {

    protected final BinaryStreamer binaryStreamer;

    public ExpandedStreamer(BinaryStreamer binaryStreamer) {
        this.binaryStreamer = binaryStreamer;
    }

    @Override
    public BinaryStreamer binaryStreamer()
    {
        return binaryStreamer;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return binaryStreamer.getParent();
    }

    @Override
    public boolean delete() {
        return binaryStreamer.delete();
    }

    @Override
    public String getProtocol() {
        return binaryStreamer.getProtocol();
    }

    @Override
    public Path getPath() {
        return binaryStreamer.getPath();
    }

    @Override
    public boolean exists() {
        return binaryStreamer.exists();
    }

    @Override
    public long getSize() {
        return binaryStreamer.getSize();
    }

    @Override
    public long lastModified() {
        return binaryStreamer.lastModified();
    }

    @Override
    public String getUrlPath() {
        return binaryStreamer.getUrlPath();
    }

    public abstract Optional<T> find(String path);

    public abstract OutputStream outputStream(boolean append);
}
