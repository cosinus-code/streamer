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
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.zip.stream.ZipStreamEntry;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class ZipStreamer<T> implements Streamer<T> {

    protected final ZipPackStreamer zipPackStreamer;

    protected final ZipStreamEntry zipEntry;

    protected ZipStreamer(ZipPackStreamer zipPackStreamer,
                          ZipStreamEntry zipEntry) {
        this.zipPackStreamer = zipPackStreamer;
        this.zipEntry = zipEntry;
    }

    @Override
    public Streamer save() {
        try (OutputStream output = zipPackStreamer.getEntryOutputStream(zipEntry)) {
            //TODO: to check if this should be closed
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return this;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public DirectoryStreamer getParent() {
        return zipEntry.getParentPath()
                .<DirectoryStreamer>map(zipPackStreamer::createDirectoryStreamer)
                .orElse(zipPackStreamer);
    }

    @Override
    public String getProtocol() {
        return ZipPacker.ZIP_PROTOCOL;
    }

    @Override
    public Path getPath() {
        return zipEntry.getPath();
    }

    @Override
    public boolean exists() {
        return zipPackStreamer.exists(getPath());
    }

    @Override
    public long getSize() {
        return zipEntry.getSize();
    }

    @Override
    public long lastModified() {
        return zipEntry.getTime();
    }

    @Override
    public String getUrlPath() {
        return zipPackStreamer.getUrlPath() + "#" + getPath();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ZipStreamer)) {
            return false;
        }

        ZipStreamer that = (ZipStreamer) other;
        return zipEntry.equals(that.zipEntry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zipEntry);
    }
}
