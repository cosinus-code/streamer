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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.error.SaveStreamerException;

import java.nio.file.Path;
import java.util.stream.Stream;

public class FileDirectoryStreamer extends FileStreamer<FileStreamer> implements DirectoryStreamer<FileStreamer> {

    public FileDirectoryStreamer(FileMainStreamer fileMainStreamer, FileHandler fileHandler, Path path) {
        super(fileMainStreamer, fileHandler, path);
    }

    @Override
    public Stream<FileStreamer> stream() {
        return fileHandler.list(file.toPath())
                .map(fileMainStreamer::create);
    }

    @Override
    public Stream<FileStreamer> flatStream(StreamerFilter streamerFilter) {
        return stream()
                .filter(streamerFilter)
                .map(Streamer::getPath)
                .flatMap(fileHandler::walk)
                .map(fileMainStreamer::create);
    }

    @Override
    public FileDirectorySaver saver(boolean append) {
        return new FileDirectorySaver();
    }

    @Override
    public DirectoryStreamer createDirectoryStreamer(Path path) {
        return new FileDirectoryStreamer(fileMainStreamer, fileHandler, path);
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return new FileBinaryStreamer(fileMainStreamer, fileHandler, path);
    }

    @Override
    public boolean rename(Path path, String newName) {
        return fileMainStreamer.rename(path, newName);
    }

    @Override
    public void execute(Path path) {
        fileMainStreamer.execute(path);
    }

    @Override
    public Streamer create(Path path) {
        return fileMainStreamer.create(path);
    }

    @Override
    public FileDirectoryStreamer save() {
        if (!file.exists() && !file.mkdirs()) {
            throw new SaveStreamerException("Failed to create directory:" + file.getPath());
        }
        return this;
    }

    @Override
    public String getType() {
        return null;
    }
}
