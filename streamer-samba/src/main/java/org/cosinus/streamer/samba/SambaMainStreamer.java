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

package org.cosinus.streamer.samba;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.streamer.network.NetworkStreamer;
import org.cosinus.streamer.network.RootNetwork;
import org.cosinus.streamer.samba.connection.SambaConnectionFactory;
import org.cosinus.streamer.samba.connection.SambaConnectionPool;
import org.cosinus.streamer.samba.connection.SambaFile;
import org.cosinus.streamer.samba.discovery.SambaDiscovery;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;
import org.cosinus.swing.error.AbortActionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.empty;
import static org.cosinus.streamer.network.NetworkConfigurationProvider.uniqueName;
import static org.cosinus.streamer.samba.SambaMainStreamer.SAMBA_NETWORK_TYPE;
import static org.cosinus.streamer.samba.connection.SambaConnection.SEPARATOR;

/**
 * The samba network type, whose hosts are shown under the "Network" root streamer.
 * <p>
 * The paths of the samba streamers start with the host name (as configured in network.json),
 * followed by the share name and the path inside the shared folder:
 * <pre>
 * Network
 *  └─ host                    {@link SambaHostStreamer}
 *      └─ share               {@link SambaFolderStreamer}
 *          └─ folder / file   {@link SambaParentStreamer} / {@link SambaBinaryStreamer}
 * </pre>
 * On refresh, the shared folders available in the local networks are discovered
 * and the new ones are added to network.json.
 */
@RootNetwork(SAMBA_NETWORK_TYPE)
@ConditionalOnProperty(name = "streamer.samba.enabled", matchIfMissing = true)
public class SambaMainStreamer implements NetworkStreamer<SambaHostStreamer> {

    public static final String SAMBA_NETWORK_TYPE = "Samba";

    public static final String SAMBA_PROTOCOL = "smb://";

    private static final Logger LOG = LogManager.getLogger(SambaMainStreamer.class);

    private static final int HOST_PATH_LEVEL = 1;

    private static final int SHARE_PATH_LEVEL = 2;

    private final NetworkConfigurationProvider networkConfigurationProvider;

    private final SambaConnectionPool sambaConnectionPool;

    private final SambaDiscovery sambaDiscovery;

    private final SambaConnectionFactory sambaConnectionFactory;

    private ParentStreamer<?> parent;

    public SambaMainStreamer(final NetworkConfigurationProvider networkConfigurationProvider,
                             final SambaConnectionPool sambaConnectionPool,
                             final SambaDiscovery sambaDiscovery,
                             final SambaConnectionFactory sambaConnectionFactory) {
        this.networkConfigurationProvider = networkConfigurationProvider;
        networkConfigurationProvider.registerHostType(SambaConfiguration.class);
        this.sambaConnectionPool = sambaConnectionPool;
        this.sambaDiscovery = sambaDiscovery;
        this.sambaConnectionFactory = sambaConnectionFactory;
    }

    @Override
    public Stream<SambaHostStreamer> stream() {
        return getSambaHostsMap().keySet()
            .stream()
            .sorted()
            .map(this::createHostStreamer);
    }

    protected Map<String, SambaConfiguration> getSambaHostsMap() {
        return networkConfigurationProvider.getHosts(SambaConfiguration.class);
    }

    /**
     * Called on refresh, in the UI thread, so it only clears the cached network hosts.
     * The discovery is requested by the "Network" root streamer, from the background loading worker.
     */
    @Override
    public void reset() {
        networkConfigurationProvider.reload();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return parent;
    }

    @Override
    public void setParent(ParentStreamer<?> parent) {
        this.parent = parent;
    }

    /**
     * Discover the shared folders available in the local networks and save the new ones in network.json.
     *
     * @return the samba hosts, including the discovered ones
     */
    @Override
    public List<SambaConfiguration> discoverNetworkHosts() {
        discoverSambaShares();
        return List.copyOf(getSambaHostsMap().values());
    }

    /**
     * Look up all the folders shared by a samba host and replace its folders list in network.json.
     * <p>
     * The configuration of the folders still shared (e.g. the remote dir) is kept.
     * The shares are listed with the credentials entered by the user for that host, if any,
     * otherwise with the configured ones. If the access is denied (e.g. the guest account is disabled),
     * the user is asked for credentials.
     *
     * @param hostName the host name
     * @return true if the folders list is up to date, false if the shares cannot be listed, the user cancelled the credentials
     * or network.json cannot be saved
     */
    public boolean refreshFolders(String hostName) {
        SambaConfiguration configuration = getSambaHostsMap().get(hostName);
        if (configuration == null) {
            return false;
        }

        try {
            List<SambaFolderConfiguration> folders = sambaConnectionFactory
                .runWithCredentials(hostName, configuration, configuration.description(), sambaDiscovery::listShares)
                .stream()
                .map(shareName -> configuration.findShare(shareName)
                    .orElseGet(() -> SambaFolderConfiguration.share(shareName)))
                .toList();
            if (folders.equals(configuration.folders())) {
                return true;
            }
            if (!networkConfigurationProvider.updateHosts(hosts ->
                hosts.put(hostName, configuration.withFolders(folders)))) {
                return false;
            }
            LOG.info("Saved {} shared folders of the samba host {}", folders.size(), hostName);
            return true;
        } catch (AbortActionException ex) {
            LOG.info("Refreshing the shared folders of the samba host {} was cancelled", hostName);
            return false;
        } catch (Exception ex) {
            LOG.error("Failed to refresh the shared folders of the samba host {}", hostName, ex);
            return false;
        }
    }

    /**
     * Search the shared folders available in the local networks and save the new ones in network.json.
     * <p>
     * The discovered shares are merged into the samba hosts from network.json, so that the configured hosts are never lost.
     * The new hosts get a "-2", "-3"... suffix if their name is already used by another host.
     */
    public void discoverSambaShares() {
        try {
            Map<String, SambaConfiguration> existing = getSambaHostsMap();
            Map<String, SambaConfiguration> discovered = sambaDiscovery.discover(existing);
            if (!discovered.equals(existing) &&
                networkConfigurationProvider.updateHosts(hosts -> addDiscoveredHosts(hosts, discovered, existing))) {
                LOG.info("Saved the discovered samba shares");
            }
        } catch (RuntimeException ex) {
            LOG.error("Failed to discover the samba shares", ex);
        }
    }

    /**
     * Add the discovered samba hosts to the network hosts, renaming the new ones whose name is already used.
     *
     * @param hosts      the network hosts, of all the network types
     * @param discovered the discovered samba hosts, including the existing ones
     * @param existing   the samba hosts already configured, which keep their names
     */
    protected void addDiscoveredHosts(Map<String, ? super SambaConfiguration> hosts,
                                      Map<String, SambaConfiguration> discovered,
                                      Map<String, SambaConfiguration> existing) {
        discovered.forEach((name, configuration) -> {
            if (existing.containsKey(name)) {
                hosts.put(name, configuration);
                return;
            }
            // the names of the other discovered hosts are reserved, so they keep their names
            Set<String> usedNames = new HashSet<>(discovered.keySet());
            usedNames.remove(name);
            usedNames.addAll(hosts.keySet());
            String uniqueName = uniqueName(name, usedNames);
            hosts.put(uniqueName, configuration.withName(uniqueName));
        });
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public String getProtocol() {
        return SAMBA_PROTOCOL;
    }

    @Override
    public Optional<SambaHostStreamer> findByPath(Path path) {
        return ofNullable(path)
            .filter(p -> p.getNameCount() > 0)
            .filter(p -> getSambaHostsMap().containsKey(hostName(p)))
            .map(this::hostName)
            .map(this::createHostStreamer);
    }

    public Streamer<?> createRoot(Path path, boolean directory) {
        if (path.getNameCount() == HOST_PATH_LEVEL) {
            return createHostStreamer(hostName(path));
        }
        if (path.getNameCount() == SHARE_PATH_LEVEL) {
            return createFolderStreamer(hostName(path), shareName(path));
        }
        return createSambaStreamer(path, directory);
    }

    /**
     * Create the streamer of a folder or a file inside a samba share, without connecting to the samba server:
     * its remote file is loaded when needed.
     *
     * @param path      the path, as host/share/inner/path
     * @param directory true for a folder, false for a file
     * @return the streamer
     */
    public SambaStreamer<?> createSambaStreamer(Path path, boolean directory) {
        return directory ?
            new SambaParentStreamer(path, null) :
            new SambaBinaryStreamer(path, null);
    }

    @Override
    public SambaHostStreamer create(Path path, boolean parent) {
        Path smbPath = path.subpath(1, path.getNameCount());
        if (smbPath.getNameCount() == HOST_PATH_LEVEL) {
            return createHostStreamer(hostName(smbPath));
        }
        if (smbPath.getNameCount() == SHARE_PATH_LEVEL) {
            return createHostStreamerWithShare(hostName(smbPath), shareName(smbPath));
        }
        return null;
    }

    public SambaStreamer<?> createFromRemote(Path path, SambaFile sambaFile) {
        return sambaFile.directory() ?
            new SambaParentStreamer(path, sambaFile) :
            new SambaBinaryStreamer(path, sambaFile);
    }

    protected SambaHostStreamer createHostStreamer(String hostName) {
        return new SambaHostStreamer(this, hostName, getSambaHostConfiguration(hostName));
    }

    protected SambaHostStreamer createHostStreamerWithShare(String hostName, String shareName) {
        SambaConfiguration hostConfiguration = getSambaHostConfiguration(hostName);
        SambaFolderConfiguration shareConfiguration = hostConfiguration.findShare(shareName)
            .orElseGet(() -> SambaFolderConfiguration.share(shareName));
        return new SambaHostStreamer(this, hostName, hostConfiguration.withFolders(List.of(shareConfiguration)));
    }

    protected SambaFolderStreamer createFolderStreamer(String hostName, String shareName) {
        SambaConfiguration hostConfiguration = getSambaHostConfiguration(hostName);
        SambaFolderConfiguration shareConfiguration = hostConfiguration.findShare(shareName)
            .orElseGet(() -> SambaFolderConfiguration.share(shareName));
        return new SambaFolderStreamer(hostName, hostConfiguration, shareConfiguration);
    }

    protected SambaConfiguration getSambaHostConfiguration(String hostName) {
        return ofNullable(getSambaHostsMap().get(hostName))
            .orElseGet(() -> new SambaConfiguration(hostName, hostName, null, null, null, null, null, null));
    }

    public String hostName(Path path) {
        return path.getName(0).toString();
    }

    public String shareName(Path path) {
        return path.getName(1).toString();
    }

    public String toInnerPath(Path path) {
        return path.getNameCount() <= SHARE_PATH_LEVEL ?
            "" :
            normalize(path.subpath(SHARE_PATH_LEVEL, path.getNameCount()).toString());
    }

    public String resolve(String innerPath, String name) {
        String normalized = normalize(innerPath);
        return normalized.isEmpty() ? name : normalized + SEPARATOR + name;
    }

    public String normalize(String innerPath) {
        return segments(innerPath).collect(joining(SEPARATOR));
    }

    public Stream<String> segments(String innerPath) {
        return innerPath == null ?
            empty() :
            Arrays.stream(innerPath.split("[/\\\\]+"))
                .filter(not(String::isEmpty));
    }

    @Override
    public boolean existHost(String hostName) {
        return getSambaHostsMap().containsKey(hostName);
    }

    /**
     * Rename a samba host, keeping its configuration and its position in network.json.
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
     * Save a new samba host in network.json.
     *
     * @param sambaHostStreamer the streamer of the new host
     * @return true if the host was saved, false if the name is used by a host of another network type
     */
    public boolean saveHost(SambaHostStreamer sambaHostStreamer) {
        return networkConfigurationProvider.saveHost(sambaHostStreamer.getHostConfiguration());
    }

    /**
     * Delete a samba host from network.json.
     *
     * @param sambaHostStreamer the streamer of the host
     * @return true if the host was deleted
     */
    public boolean deleteHost(final SambaHostStreamer sambaHostStreamer) {
        return existHost(sambaHostStreamer.getName()) &&
            networkConfigurationProvider.deleteHost(sambaHostStreamer.getName());
    }
}
