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

package org.cosinus.streamer.file;

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public abstract class FileStreamer<T> implements Streamer<T> {

    protected final FileMainStreamer fileMainStreamer;

    protected final FileHandler fileHandler;

    protected final File file;

    public FileStreamer(FileMainStreamer fileMainStreamer, FileHandler fileHandler, Path path) {
        this.fileMainStreamer = fileMainStreamer;
        this.fileHandler = fileHandler;
        this.file = path.toFile();
    }

    public File getFile() {
        return file;
    }

    @Override
    public Path getPath() {
        return file.toPath();
    }

    @Override
    public DirectoryStreamer getParent() {
        return ofNullable(file.toPath().getParent())
            .map(parentPath -> fileMainStreamer
                .stream()
                .filter(fileStreamer -> fileStreamer.getPath().equals(parentPath))
                .findFirst()
                .map(DirectoryStreamer.class::cast)
                .orElseGet(() -> new FileDirectoryStreamer(fileMainStreamer, fileHandler, parentPath)))
            .orElse(fileMainStreamer);
    }

    @Override
    public DirectoryStreamer getRootStreamer() {
        return fileMainStreamer
            .getRoots()
            .stream()
            .filter(streamer -> getPath().startsWith(streamer.getPath()))
            .findFirst()
            .orElse(null);
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
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        return file.canWrite();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public boolean delete() {
        return file.delete() && !file.exists();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public String getProtocol() {
        return FileMainStreamer.FILE_PROTOCOL;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FileStreamer)) {
            return false;
        }

        FileStreamer that = (FileStreamer) other;
        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
