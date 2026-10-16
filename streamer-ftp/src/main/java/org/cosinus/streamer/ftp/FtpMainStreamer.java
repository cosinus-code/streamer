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
package org.cosinus.streamer.ftp;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.streamer.network.NetworkStreamer;
import org.cosinus.streamer.network.RootNetwork;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ftp.FtpMainStreamer.FTP_NETWORK_TYPE;

/**
 * The ftp network type, whose hosts are shown under the "Network" root streamer.
 * <p>
 * An ftp host has no folders level, so it streams directly the remote files:
 * <pre>
 * Network
 *  └─ host                    {@link FtpHostStreamer}
 *      └─ folder / file       {@link FtpParentStreamer} / {@link FtpBinaryStreamer}
 * </pre>
 */
@RootNetwork(FTP_NETWORK_TYPE)
@ConditionalOnProperty(name = "streamer.ftp.enabled", matchIfMissing = true)
public class FtpMainStreamer implements NetworkStreamer<FtpHostStreamer> {

    public static final String FTP_NETWORK_TYPE = "Ftp";

    public static final String FTP_PROTOCOL = "ftp://";

    private static final int HOST_PATH_LEVEL = 1;

    private final NetworkConfigurationProvider networkConfigurationProvider;

    private ParentStreamer<?> parent;

    public FtpMainStreamer(final NetworkConfigurationProvider networkConfigurationProvider) {
        this.networkConfigurationProvider = networkConfigurationProvider;
        networkConfigurationProvider.registerHostType(FtpConfiguration.class);
    }

    @Override
    public Stream<FtpHostStreamer> stream() {
        return getFtpHostsMap().keySet()
            .stream()
            .sorted()
            .map(FtpHostStreamer::new);
    }

    protected Map<String, FtpConfiguration> getFtpHostsMap() {
        return networkConfigurationProvider.getHosts(FtpConfiguration.class);
    }

    /**
     * Called on refresh, in the UI thread, so it only clears the cached network hosts.
     */
    @Override
    public void reset() {
        networkConfigurationProvider.reload();
    }

    /**
     * The ftp hosts are not discovered in the local networks.
     *
     * @return the configured ftp hosts
     */
    @Override
    public List<FtpConfiguration> discoverNetworkHosts() {
        return List.copyOf(getFtpHostsMap().values());
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
    public Path getPath() {
        return null;
    }

    @Override
    public String getProtocol() {
        return FTP_PROTOCOL;
    }

    @Override
    public boolean existHost(String hostName) {
        return getFtpHostsMap().containsKey(hostName);
    }

    /**
     * Find an ftp host by its path, as in its url.
     *
     * @param path the path, with the host name
     * @return the host streamer, or empty if the host is not configured or the path is deeper than the host
     */
    @Override
    public Optional<FtpHostStreamer> findByPath(Path path) {
        return ofNullable(path)
            .filter(p -> p.getNameCount() == HOST_PATH_LEVEL)
            .map(Path::toString)
            .filter(this::existHost)
            .map(FtpHostStreamer::new);
    }

    /**
     * Create the streamer of an ftp host, which may be new.
     *
     * @param path      the path, starting with the "Network" root, then the host name
     * @param directory not used, as a host is always a directory
     * @return the host streamer, or null for a path deeper than the host
     */
    @Override
    public FtpHostStreamer create(Path path, boolean directory) {
        return path.getNameCount() == HOST_PATH_LEVEL + 1 ?
            new FtpHostStreamer(path.getFileName().toString()) :
            null;
    }

    /**
     * @param hostName the host name
     * @return the configuration of the host, or a new one using the host name as address
     */
    public FtpConfiguration getFtpHostConfiguration(String hostName) {
        return ofNullable(getFtpHostsMap().get(hostName))
            .orElseGet(() -> FtpConfiguration.host(hostName));
    }

    /**
     * Save a new ftp host in network.json.
     *
     * @param hostName the host name
     * @return true if the host was saved, false if the name is used by a host of another network type
     */
    public boolean saveHost(String hostName) {
        return networkConfigurationProvider.saveHost(getFtpHostConfiguration(hostName));
    }

    /**
     * Rename an ftp host, keeping its configuration and its position in network.json.
     *
     * @param hostName    the current host name
     * @param newHostName the new host name
     * @return true if the host was renamed, false if the new name is empty, the same or already used,
     * or if network.json cannot be saved
     */
    public boolean renameHost(String hostName, String newHostName) {
        return existHost(hostName) &&
            networkConfigurationProvider.renameHost(hostName, newHostName);
    }

    /**
     * Delete an ftp host from network.json.
     *
     * @param hostName the host name
     * @return true if the host was deleted
     */
    public boolean deleteHost(String hostName) {
        return existHost(hostName) &&
            networkConfigurationProvider.deleteHost(hostName);
    }
}
