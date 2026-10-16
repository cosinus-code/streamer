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

package org.cosinus.streamer.samba.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.streamer.network.NetworkHost;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;

/**
 * Network configuration provider returning the hosts set by the test, instead of reading network.json,
 * and keeping the saved ones in memory, instead of writing network.json.
 */
public class TestNetworkConfigurationProvider extends NetworkConfigurationProvider {

    private Map<String, NetworkHost> hosts = Map.of();

    public TestNetworkConfigurationProvider() {
        super(new ObjectMapper(), Set.of());
    }

    /**
     * Replace all the network hosts.
     *
     * @param hosts the hosts, by name
     */
    public void setHosts(Map<String, ? extends NetworkHost> hosts) {
        this.hosts = unmodifiableMap(new LinkedHashMap<>(hosts));
        reload();
    }

    @Override
    protected Map<String, NetworkHost> readHosts() {
        return hosts;
    }

    @Override
    protected void writeHosts(Map<String, NetworkHost> hosts) {
        this.hosts = hosts;
    }
}
