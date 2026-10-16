/*
 * Copyright 2026 Cosinus Software
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


package org.cosinus.streamer.network;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Network type with the hosts set by the test, recording the calls of the "Network" root streamer.
 */
public class TestNetworkStreamer implements NetworkStreamer<TestNetworkStreamer.TestHostStreamer> {

    private final String type;

    private final String protocol;

    private final List<String> hostNames;

    private final List<Path> createdPaths = new ArrayList<>();

    private ParentStreamer<?> parent;

    private int discoveries;

    private int resets;

    public TestNetworkStreamer(String type, String protocol, String... hostNames) {
        this.type = type;
        this.protocol = protocol;
        this.hostNames = List.of(hostNames);
    }

    /**
     * @return the type set by the test, otherwise the one from the {@link RootNetwork} annotation
     */
    @Override
    public String getType() {
        return type != null ? type : NetworkStreamer.super.getType();
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return parent;
    }

    @Override
    public void setParent(ParentStreamer<?> parent) {
        this.parent = parent;
    }

    @Override
    public Stream<TestHostStreamer> stream() {
        return hostNames.stream().map(this::host);
    }

    @Override
    public List<NetworkHost> discoverNetworkHosts() {
        discoveries++;
        return List.of();
    }

    @Override
    public boolean existHost(String hostName) {
        return hostNames.contains(hostName);
    }

    @Override
    public Optional<TestHostStreamer> findByPath(Path path) {
        return path.getNameCount() > 0 && existHost(path.getName(0).toString()) ?
            Optional.of(host(path.toString())) :
            Optional.empty();
    }

    @Override
    public TestHostStreamer create(Path path, boolean directory) {
        createdPaths.add(path);
        return host(path.getFileName().toString());
    }

    @Override
    public void reset() {
        resets++;
    }

    public TestHostStreamer host(String path) {
        return new TestHostStreamer(Paths.get(path), protocol, parent);
    }

    public List<Path> getCreatedPaths() {
        return createdPaths;
    }

    public int getDiscoveries() {
        return discoveries;
    }

    public int getResets() {
        return resets;
    }

    public record TestHostStreamer(Path path, String protocol, ParentStreamer<?> parent)
        implements NetworkHostStreamer<Streamer<?>> {

        @Override
        public Stream<Streamer<?>> stream() {
            return Stream.empty();
        }

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }

        @Override
        public ParentStreamer<?> getParent() {
            return parent;
        }

        @Override
        public NetworkHost getNetworkHost() {
            return null;
        }
    }
}
