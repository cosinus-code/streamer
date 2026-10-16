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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkConfigurationProviderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String networkJson;

    private NetworkConfigurationProvider provider;

    @Before
    public void createProvider() {
        // the ftp type is not registered, so its hosts are unknown
        networkJson = """
            {
              "nas": {"type": "samba", "name": "nas", "host": "nas.lan"},
              "rebex": {"type": "ftp", "name": "rebex", "host": "test.rebex.net", "port": 21},
              "office": {"type": "samba", "name": "office", "host": "office.lan"}
            }""";

        // network.json kept in memory, instead of reading and writing the file
        provider = new NetworkConfigurationProvider(objectMapper, Set.of()) {
            @Override
            protected Map<String, NetworkHost> readHosts() {
                return toModelsMap(networkJson.getBytes(UTF_8));
            }

            @Override
            protected void writeHosts(Map<String, NetworkHost> hosts) throws IOException {
                networkJson = objectMapper.writeValueAsString(hosts);
            }
        };
        provider.registerHostType(SambaHost.class);
    }

    @Test
    public void testHostsOfType() {
        assertEquals(List.of("nas", "office"), List.copyOf(provider.getHosts(SambaHost.class).keySet()));
        assertEquals(new SambaHost("nas", "nas.lan"), provider.getHost("nas", SambaHost.class).orElseThrow());
        assertTrue(provider.getHost("rebex", SambaHost.class).isEmpty());
    }

    @Test
    public void testHostOfUnregisteredTypeIsUnknown() {
        NetworkHost rebex = provider.getConnectionModelsMap().get("rebex");

        assertTrue(rebex instanceof UnknownNetworkHost);
        assertEquals("ftp", rebex.type());
        assertEquals("rebex", rebex.name());
        assertEquals("test.rebex.net", rebex.host());
    }

    @Test
    public void testSaveWritesAllTheHosts() throws IOException {
        assertTrue(provider.saveHost(new SambaHost("office", "office.home")));
        assertTrue(provider.saveHost(new SambaHost("media", "media.lan")));

        // the replaced host stays in place, the new one is added at the end and the unknown one is kept unchanged
        Map<String, JsonNode> saved = savedJson();
        assertEquals(List.of("nas", "rebex", "office", "media"), List.copyOf(saved.keySet()));
        assertEquals(json("{\"type\": \"ftp\", \"name\": \"rebex\", \"host\": \"test.rebex.net\", \"port\": 21}"),
            saved.get("rebex"));
        assertEquals(json("{\"type\": \"samba\", \"name\": \"office\", \"host\": \"office.home\"}"),
            saved.get("office"));

        provider.reload();
        assertEquals(new SambaHost("media", "media.lan"), provider.getHost("media", SambaHost.class).orElseThrow());
    }

    @Test
    public void testSaveRefusesNameOfAnotherType() {
        String before = networkJson;

        assertFalse(provider.saveHost(new SambaHost("rebex", "nas.lan")));
        assertEquals(before, networkJson);
    }

    @Test
    public void testRenameKeepsThePosition() throws IOException {
        assertTrue(provider.renameHost("nas", "media"));

        assertEquals(Set.of("media", "rebex", "office"), savedJson().keySet());
        assertEquals(new SambaHost("media", "nas.lan"), provider.getHost("media", SambaHost.class).orElseThrow());
    }

    @Test
    public void testRenameToUsedOrInvalidNameFails() {
        String before = networkJson;

        assertFalse(provider.renameHost("nas", "rebex"));
        assertFalse(provider.renameHost("nas", "nas"));
        assertFalse(provider.renameHost("nas", " "));
        assertFalse(provider.renameHost("missing", "other"));
        assertEquals(before, networkJson);
    }

    @Test
    public void testRenameUnknownHost() {
        assertTrue(provider.renameHost("rebex", "demo"));

        assertEquals("demo", provider.getConnectionModelsMap().get("demo").name());
    }

    @Test
    public void testDeleteHost() throws IOException {
        assertTrue(provider.deleteHost("rebex"));
        assertFalse(provider.deleteHost("rebex"));

        assertEquals(List.of("nas", "office"), List.copyOf(savedJson().keySet()));
    }

    @Test
    public void testUniqueName() {
        assertEquals("media", NetworkConfigurationProvider.uniqueName("media", Set.of("nas")));
        assertEquals("nas-2", NetworkConfigurationProvider.uniqueName("nas", Set.of("nas")));
        assertEquals("nas-3", NetworkConfigurationProvider.uniqueName("nas", Set.of("nas", "nas-2")));
    }

    private Map<String, JsonNode> savedJson() throws IOException {
        return objectMapper.readValue(networkJson, new TypeReference<LinkedHashMap<String, JsonNode>>() {
        });
    }

    private JsonNode json(String json) throws IOException {
        return objectMapper.readTree(json);
    }

    @JsonTypeName("samba")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SambaHost(String name, String host) implements NetworkHost {

        @Override
        public String type() {
            return "samba";
        }

        @Override
        public SambaHost withName(String newName) {
            return new SambaHost(newName, host);
        }
    }
}
