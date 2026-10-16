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

package org.cosinus.streamer.samba.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.cosinus.streamer.samba.model.SambaConfiguration.DEFAULT_CONNECTION_TIMEOUT;
import static org.cosinus.streamer.samba.model.SambaConfiguration.DEFAULT_PORT;
import static org.cosinus.streamer.samba.model.SambaFolderConfiguration.share;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SambaConfigurationTest {

    private static final String SAMBA_JSON = """
        {
          "station": {
            "host": "192.168.1.1",
            "username": "user",
            "password": "secret",
            "domain": "WORKGROUP",
            "folders": [
              { "name": "seriale" },
              { "name": "filme-2000", "remoteDir": "2024" }
            ]
          },
          "nas": {
            "host": "nas.local",
            "port": 1445
          }
        }
        """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testDefaultValues() {
        SambaConfiguration configuration = host("host", null);

        assertNull(configuration.port());
        assertNull(configuration.connectionTimeout());
        assertEquals(DEFAULT_PORT, configuration.portOrDefault());
        assertEquals(DEFAULT_CONNECTION_TIMEOUT, configuration.connectionTimeoutOrDefault());
        assertEquals(List.of(), configuration.folders());
    }

    @Test
    public void testExplicitValuesAreKept() {
        SambaConfiguration configuration =
            new SambaConfiguration("nas", "host", 1445, null, null, null, 5, List.of(share("public")));

        assertEquals(Integer.valueOf(1445), configuration.port());
        assertEquals(Integer.valueOf(5), configuration.connectionTimeout());
        assertEquals(1445, configuration.portOrDefault());
        assertEquals(5, configuration.connectionTimeoutOrDefault());
        assertEquals(List.of(share("public")), configuration.folders());
    }

    @Test
    public void testFindShareIgnoresCase() {
        SambaConfiguration configuration = host("host", List.of(share("Public"), share("media")));

        assertEquals(share("Public"), configuration.findShare("public").orElseThrow());
        assertEquals(share("media"), configuration.findShare("MEDIA").orElseThrow());
        assertTrue(configuration.findShare("other").isEmpty());
    }

    @Test
    public void testDescription() {
        SambaConfiguration configuration = host("192.168.1.10", null);

        assertEquals("\\\\192.168.1.10", configuration.description());
        assertEquals("\\\\192.168.1.10\\public", configuration.description("public"));
    }

    @Test
    public void testReadSambaJson() throws IOException {
        Map<String, SambaConfiguration> hosts = objectMapper.readValue(SAMBA_JSON, new TypeReference<>() {
        });

        assertEquals(List.of("station", "nas"), List.copyOf(hosts.keySet()));

        SambaConfiguration station = hosts.get("station");
        assertEquals("192.168.1.1", station.host());
        assertNull(station.port());
        assertEquals("user", station.username());
        assertEquals("WORKGROUP", station.domain());
        assertEquals(
            asList(share("seriale"), new SambaFolderConfiguration("filme-2000", "2024")),
            station.folders());

        SambaConfiguration nas = hosts.get("nas");
        assertEquals(Integer.valueOf(1445), nas.port());
        assertEquals(List.of(), nas.folders());
    }

    @Test
    public void testWriteAndReadSambaJson() throws IOException {
        Map<String, SambaConfiguration> hosts = Map.of("station",
            new SambaConfiguration("station", "192.168.1.1", null, "WORKGROUP", "user", "secret", null,
                asList(share("seriale"), new SambaFolderConfiguration("filme-2000", "2024"))));

        String json = objectMapper.writeValueAsString(hosts);
        Map<String, SambaConfiguration> readHosts = objectMapper.readValue(json, new TypeReference<>() {
        });

        assertEquals(hosts, readHosts);
    }

    private static SambaConfiguration host(String host, List<SambaFolderConfiguration> shares) {
        return new SambaConfiguration(null, host, null, null, null, null, null, shares);
    }
}
