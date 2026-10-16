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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.stream.FlatStreamingStrategy;
import org.cosinus.stream.StreamingStrategy;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.api.value.TranslatableName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.streamer.api.value.TranslatableName.translatableNames;
import static org.cosinus.swing.image.icon.IconProvider.ICON_NETWORK;

/**
 * The "Network" root streamer, showing the hosts of all the network types (e.g. samba, ftp).
 * <p>
 * Each network type is a {@link NetworkStreamer} bean, annotated with {@link RootNetwork}.
 * The streamers of a host keep the protocol of its network type (e.g. "smb://", "ftp://"),
 * so their urls are routed to that network type.
 */
@RootStreamer(NetworkMainStreamer.NETWORK)
public class NetworkMainStreamer extends MainStreamer<Streamer<?>> {

    public static final String NETWORK = "Network";

    public static final String NETWORK_PROTOCOL = "network://";

    private static final Logger LOG = LogManager.getLogger(NetworkMainStreamer.class);

    /**
     * The protocol of a location entered as "//host" or "//host/share".
     */
    public static final String SAMBA_PROTOCOL = "smb://";

    private static final String PROTOCOL_SEPARATOR = "//";

    private final List<NetworkStreamer> networkStreamers;

    private volatile boolean discoveryRequested;

    private List<TranslatableName> detailNames;

    public NetworkMainStreamer(final List<NetworkStreamer> networkStreamers) {
        this.networkStreamers = List.copyOf(networkStreamers);
        this.networkStreamers.forEach(networkStreamer -> networkStreamer.setParent(this));
    }

    /**
     * Stream the hosts of all the network types, sorted by name.
     * <p>
     * After a refresh, the hosts are first discovered in the local networks.
     */
    @Override
    public Stream<Streamer<?>> stream() {
        if (discoveryRequested) {
            discoveryRequested = false;
            networkStreamers.forEach(this::discoverNetworkHosts);
        }
        return networkStreamers.stream()
            .flatMap(NetworkStreamer::stream)
            .map(Streamer.class::cast);
    }

    private void discoverNetworkHosts(NetworkStreamer networkStreamer) {
        try {
            networkStreamer.discoverNetworkHosts();
        } catch (RuntimeException ex) {
            LOG.error("Failed to discover the {} hosts", networkStreamer.getType(), ex);
        }
    }

    @Override
    public Stream<Streamer<?>> flatStream(final FlatStreamingStrategy flatStreamingStrategy,
                                          final StreamingStrategy streamingStrategy,
                                          final StreamerFilter streamerFilter) {
        return stream().filter(streamerFilter);
    }

    /**
     * Called on refresh, which happens in the UI thread.
     * Only mark the discovery as requested, to be performed by the next {@link #stream()},
     * which is called by the background loading worker.
     */
    @Override
    public void reset() {
        discoveryRequested = true;
        networkStreamers.forEach(NetworkStreamer::reset);
    }

    /**
     * @return the network types
     */
    public List<NetworkStreamer> getNetworkStreamers() {
        return networkStreamers;
    }

    @Override
    public String getProtocol() {
        return NETWORK_PROTOCOL;
    }

    /**
     * @param urlPath the url path
     * @return true for the "Network" url, or for the url of a network type (e.g. "smb://...")
     */
    @Override
    public boolean isCompatible(String urlPath) {
        return super.isCompatible(urlPath) || findNetworkStreamerForUrl(urlPath).isPresent();
    }

    @Override
    public Optional<Streamer<?>> findByUrlPath(String urlPath) {
        Optional<NetworkStreamer> networkStreamer = findNetworkStreamerForUrl(urlPath);
        if (networkStreamer.isEmpty()) {
            return super.findByUrlPath(urlPath);
        }
        Path path = Paths.get(urlPath.substring(networkStreamer.get().getProtocol().length()));
        // an unknown host of a known protocol opens the network root
        Streamer<?> streamer = (Streamer<?>) networkStreamer.get()
            .findByPath(path)
            .orElse(this);
        return Optional.of(streamer);
    }

    private Optional<NetworkStreamer> findNetworkStreamerForUrl(String urlPath) {
        return Optional.ofNullable(urlPath)
            .flatMap(url -> networkStreamers.stream()
                .filter(networkStreamer -> url.startsWith(networkStreamer.getProtocol()))
                .findFirst());
    }

    /**
     * Create the streamer of a new host, by the network type given by the protocol of the entered location.
     * <p>
     * The entered location has the form "ftp://host", "smb://host", "smb://host/share",
     * or "//host" and "//host/share", which are samba locations.
     *
     * @param path      the "Network" root path resolved with the entered location
     * @param directory true to create a directory streamer
     * @return the streamer, or null if the location doesn't match the pattern, or has an unknown protocol
     */
    @Override
    public Streamer<?> create(Path path, boolean directory) {
        if (path == null) {
            return null;
        }
        // "//host" is an absolute path, so resolving it against the root gives "/host"
        if (path.isAbsolute()) {
            return create(SAMBA_PROTOCOL, path.getRoot().relativize(path), directory);
        }
        // "ftp://host" is normalized to "ftp:/host", so the protocol is the first element after the root
        Path location = path.startsWith(getPath()) ? getPath().relativize(path) : path;
        if (location.getNameCount() < 2 || location.getName(0).toString().isEmpty()) {
            return null;
        }
        String protocol = location.getName(0) + PROTOCOL_SEPARATOR;
        return create(protocol, location.subpath(1, location.getNameCount()), directory);
    }

    private Streamer<?> create(String protocol, Path hostPath, boolean directory) {
        if (hostPath.toString().isEmpty()) {
            return null;
        }
        return findNetworkStreamerByProtocol(protocol)
            .map(networkStreamer -> networkStreamer.create(getPath().resolve(hostPath), directory))
            .orElse(null);
    }

    private Optional<NetworkStreamer> findNetworkStreamerByProtocol(String protocol) {
        return networkStreamers.stream()
            .filter(networkStreamer -> networkStreamer.getProtocol().equalsIgnoreCase(protocol))
            .findFirst();
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public List<TranslatableName> detailNames() {
        if (detailNames == null) {
            detailNames = translatableNames(
                DETAIL_KEY_NAME,
                DETAIL_KEY_HOST,
                DETAIL_KEY_TYPE);
        }
        return detailNames;
    }

    @Override
    public String getIconName() {
        return ICON_NETWORK;
    }
}
