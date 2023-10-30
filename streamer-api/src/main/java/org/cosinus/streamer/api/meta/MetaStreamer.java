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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MetaStreamer implements ParentStreamer<MainStreamer>
{

    public static final String META_PROTOCOL = "meta://";

    private final List<MainStreamer> mainStreamers;

    public MetaStreamer(List<MainStreamer> mainStreamers) {
        this.mainStreamers = mainStreamers;
        mainStreamers.forEach(mainStreamer -> mainStreamer.setParent(this));
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public String getUrlPath() {
        return null;
    }

    @Override
    public String getName() {
        return "";
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
    public boolean canRead() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public Stream<MainStreamer> stream() {
        return mainStreamers.stream();
    }

    @Override
    public ParentStreamer getParent() {
        return null;
    }

    @Override
    public ParentStreamer<MainStreamer> createParent(Path path) {
        return null;
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return null;
    }

    @Override
    public boolean rename(Path path, String newName) {
        return false;
    }

    @Override
    public void execute(Path path) {
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public String getProtocol() {
        return META_PROTOCOL;
    }

    @Override
    public Stream<MainStreamer> flatStream(StreamerFilter streamerFilter) {
        return null;
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
    public MainStreamer create(Path path, boolean parent) {
        return null;
    }

    @Override
    public MetaStreamer save() {
        return this;
    }

    public Optional<MainStreamer> find(Path path) {
        return stream()
            .filter(mainStreamer -> mainStreamer.getPath().equals(path))
            .findFirst();
    }

    public Optional<Streamer> findByUrlPath(String urlPath) {
        return Optional.ofNullable(urlPath)
            .filter(path -> path.startsWith(META_PROTOCOL))
            .map(path -> path.substring(META_PROTOCOL.length()))
            .map(Paths::get)
            .flatMap(this::find);
    }

}
