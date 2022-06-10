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

package org.cosinus.streamer.pack.compress;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CompressStreamer implements BinaryStreamer {

    private final CompressPackStreamer compressPackStreamer;

    private final CompressMetadata metadata;

    private final CompressorInputStream compressorInputStream;

    public CompressStreamer(CompressPackStreamer compressPackStreamer,
                            CompressMetadata metadata,
                            CompressorInputStream compressorInputStream) {
        this.compressPackStreamer = compressPackStreamer;
        this.metadata = metadata;
        this.compressorInputStream = compressorInputStream;
    }

    @Override
    public InputStream inputStream() {
        return compressPackStreamer.createCompressorInputStream();
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return null;
    }

    @Override
    public Streamer<byte[]> save() {
        return null;
    }

    @Override
    public DirectoryStreamer getParent() {
        return compressPackStreamer;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public String getProtocol() {
        return compressPackStreamer.getProtocol();
    }

    @Override
    public long getFreeSpace() {
        return compressPackStreamer.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return compressPackStreamer.getTotalSpace();
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public Path getPath() {
        return Paths.get(getName());
    }

    @Override
    public String getUrlPath() {
        return compressPackStreamer.getUrlPath() + "#" + getPath();
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public long getSize() {
        return metadata.getSize();
    }

    @Override
    public long lastModified() {
        return metadata.getModificationTime();
    }
}
