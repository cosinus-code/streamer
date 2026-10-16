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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.JsonConnectionModelProvider;
import org.cosinus.swing.resource.ResourceResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 * Provider of the network hosts configured in network.json, of all the network types.
 * <p>
 * All the hosts are kept in a single map, by name, so a host name is unique across the network types.
 * Each host has a "type" (e.g. "samba", "ftp") which selects its configuration class,
 * registered by its network type with {@link #registerHostType(Class)}.
 * <p>
 * This is the only place where network.json is read and written: the whole file is saved on every update,
 * so the hosts of the other network types are always kept.
 */
@Component
public class NetworkConfigurationProvider extends JsonConnectionModelProvider<NetworkHost> {

    private static final Logger LOG = LogManager.getLogger(NetworkConfigurationProvider.class);

    public static final String NETWORK_CONNECTIONS_FILE_NAME = "network.json";

    private volatile Map<String, NetworkHost> hosts;

    public NetworkConfigurationProvider(final ObjectMapper objectMapper,
                                        final Set<ResourceResolver> resourceResolvers) {
        super(objectMapper, NetworkHost.class, resourceResolvers);
    }

    /**
     * @return all the hosts, by name, read from network.json on the first call after a {@link #reload()}
     */
    @Override
    public Map<String, NetworkHost> getConnectionModelsMap() {
        if (hosts == null) {
            hosts = ofNullable(readHosts())
                .map(map -> unmodifiableMap(new LinkedHashMap<>(map)))
                .orElseGet(Map::of);
        }
        return hosts;
    }

    /**
     * Clear the cached hosts, to be read again from network.json.
     */
    public void reload() {
        hosts = null;
    }

    /**
     * @param hostClass the configuration class of the hosts of a network type
     * @param <H>       the configuration type of the hosts
     * @return the hosts of that network type, by name
     */
    public <H extends NetworkHost> Map<String, H> getHosts(Class<H> hostClass) {
        Map<String, H> typedHosts = new LinkedHashMap<>();
        getConnectionModelsMap().forEach((name, host) -> {
            if (hostClass.isInstance(host)) {
                typedHosts.put(name, hostClass.cast(host));
            }
        });
        return unmodifiableMap(typedHosts);
    }

    /**
     * @param name      the host name
     * @param hostClass the configuration class of the hosts of a network type
     * @param <H>       the configuration type of the hosts
     * @return the host, if it exists and is of that network type
     */
    public <H extends NetworkHost> Optional<H> getHost(String name, Class<H> hostClass) {
        return ofNullable(getConnectionModelsMap().get(name))
            .filter(hostClass::isInstance)
            .map(hostClass::cast);
    }

    /**
     * Update the hosts and save them all in network.json.
     *
     * @param update the update of the hosts, by name, keeping their order
     * @return true if the hosts were saved
     */
    public synchronized boolean updateHosts(Consumer<Map<String, NetworkHost>> update) {
        Map<String, NetworkHost> updated = new LinkedHashMap<>(getConnectionModelsMap());
        update.accept(updated);
        try {
            writeHosts(unmodifiableMap(updated));
            hosts = unmodifiableMap(updated);
            return true;
        } catch (IOException | RuntimeException ex) {
            LOG.error("Failed to save the network hosts in {}", NETWORK_CONNECTIONS_FILE_NAME, ex);
            return false;
        }
    }

    /**
     * Save a host, new or replacing the one of the same network type with the same name.
     *
     * @param host the host to save
     * @return true if the host was saved, false if its name is used by a host of another network type
     */
    public synchronized boolean saveHost(NetworkHost host) {
        NetworkHost existing = getConnectionModelsMap().get(host.name());
        if (existing != null && !Objects.equals(existing.type(), host.type())) {
            LOG.warn("The network host name {} is already used by a {} host", host.name(), existing.type());
            return false;
        }
        return updateHosts(updated -> updated.put(host.name(), host));
    }

    /**
     * Rename a host, keeping its position in network.json.
     *
     * @param name    the current host name
     * @param newName the new host name
     * @return true if the host was renamed, false if it doesn't exist,
     * or if the new name is empty, the same or already used by any host
     */
    public synchronized boolean renameHost(String name, String newName) {
        Map<String, NetworkHost> existing = getConnectionModelsMap();
        if (newName == null ||
            newName.isBlank() ||
            newName.equals(name) ||
            !existing.containsKey(name) ||
            existing.containsKey(newName)) {
            return false;
        }

        return updateHosts(updated ->
            updated.put(newName, updated.remove(name).withName(newName)));
    }

    /**
     * @param name the host name
     * @return true if the host was deleted, false if it doesn't exist or network.json cannot be saved
     */
    public synchronized boolean deleteHost(String name) {
        return getConnectionModelsMap().containsKey(name) &&
            updateHosts(updated -> updated.remove(name));
    }

    /**
     * @param name      the wanted host name
     * @param usedNames the host names already used
     * @return the wanted name if not used, otherwise the name followed by the first free "-2", "-3"... suffix
     */
    public static String uniqueName(String name, Set<String> usedNames) {
        String uniqueName = name;
        for (int index = 2; usedNames.contains(uniqueName); index++) {
            uniqueName = name + "-" + index;
        }
        return uniqueName;
    }

    /**
     * @return the hosts from network.json, or null if network.json doesn't exist
     */
    protected Map<String, NetworkHost> readHosts() {
        return super.getConnectionModelsMap();
    }

    /**
     * @param hosts all the hosts to save in network.json
     * @throws IOException if the file cannot be written
     */
    protected void writeHosts(Map<String, NetworkHost> hosts) throws IOException {
        saveModelsMap(getJsonFileName(), hosts);
    }

    @Override
    protected String getJsonFileName() {
        return NETWORK_CONNECTIONS_FILE_NAME;
    }
}
