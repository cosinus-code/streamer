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


package org.cosinus.streamer.ftp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.streamer.network.NetworkHost;
import org.cosinus.streamer.network.UnknownNetworkHost;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FtpMainStreamerTest {

    private Map<String, NetworkHost> savedHosts;

    private FtpMainStreamer ftpMainStreamer;

    @Before
    public void createFtpMainStreamer() {
        savedHosts = new LinkedHashMap<>();
        savedHosts.put("rebex", new FtpConfiguration("rebex", "test.rebex.net", 2121, "demo", "password",
            10, true, null, null, null, 0, null));
        savedHosts.put("office", FtpConfiguration.host("office"));

        // the hosts of network.json, kept in memory
        ftpMainStreamer = new FtpMainStreamer(new NetworkConfigurationProvider(new ObjectMapper(), Set.of()) {
            @Override
            protected Map<String, NetworkHost> readHosts() {
                return savedHosts;
            }

            @Override
            protected void writeHosts(Map<String, NetworkHost> hosts) {
                savedHosts = new LinkedHashMap<>(hosts);
            }
        });
    }

    @Test
    public void testConfigurationKeepsSetValuesAndDefaultsTheOthers() {
        FtpConfiguration rebex = (FtpConfiguration) savedHosts.get("rebex");
        assertEquals(2121, rebex.port());
        assertEquals(10, rebex.connectionTimeout());
        assertEquals(FtpConfiguration.DEFAULT_KEEP_ALIVE_TIMEOUT, rebex.keepAliveTimeout());
        assertEquals(FtpConfiguration.DEFAULT_ENCODING, rebex.encoding());

        FtpConfiguration office = (FtpConfiguration) savedHosts.get("office");
        assertEquals("office", office.host());
        assertEquals(FtpConfiguration.DEFAULT_PORT, office.port());
        assertEquals(FtpConfiguration.DEFAULT_CONNECTION_TIMEOUT, office.connectionTimeout());
    }

    @Test
    public void testDiscoverReturnsTheConfiguredHosts() {
        assertEquals(List.copyOf(savedHosts.values()), ftpMainStreamer.discoverNetworkHosts());
        assertTrue(ftpMainStreamer.existHost("rebex"));
        assertFalse(ftpMainStreamer.existHost("nas"));
    }

    @Test
    public void testSaveNewHost() {
        assertTrue(ftpMainStreamer.saveHost("media"));

        assertEquals(List.of("rebex", "office", "media"), List.copyOf(savedHosts.keySet()));
        assertEquals(FtpConfiguration.host("media"), savedHosts.get("media"));
    }

    @Test
    public void testRenameHost() {
        assertTrue(ftpMainStreamer.renameHost("rebex", "demo"));

        assertEquals(Set.of("demo", "office"), savedHosts.keySet());
        assertEquals("demo", savedHosts.get("demo").name());
        assertEquals("test.rebex.net", savedHosts.get("demo").host());
        assertTrue(ftpMainStreamer.existHost("demo"));
    }

    @Test
    public void testRenameHostToUsedOrInvalidNameFails() {
        assertFalse(ftpMainStreamer.renameHost("rebex", "office"));
        assertFalse(ftpMainStreamer.renameHost("rebex", " "));
        assertFalse(ftpMainStreamer.renameHost("missing", "other"));
        assertEquals(List.of("rebex", "office"), List.copyOf(savedHosts.keySet()));
    }

    @Test
    public void testDeleteHost() {
        assertTrue(ftpMainStreamer.deleteHost("office"));
        assertFalse(ftpMainStreamer.deleteHost("office"));

        assertEquals(List.of("rebex"), List.copyOf(savedHosts.keySet()));
    }

    @Test
    public void testHostsOfOtherNetworkTypesAreKept() {
        savedHosts.put("nas", new UnknownNetworkHost("samba", "nas", "nas.lan"));

        assertFalse(ftpMainStreamer.existHost("nas"));
        assertFalse(ftpMainStreamer.saveHost("nas"));
        assertFalse(ftpMainStreamer.renameHost("rebex", "nas"));
        assertFalse(ftpMainStreamer.deleteHost("nas"));
        assertEquals(2, ftpMainStreamer.discoverNetworkHosts().size());

        assertTrue(ftpMainStreamer.saveHost("media"));
        assertEquals(List.of("rebex", "office", "nas", "media"), List.copyOf(savedHosts.keySet()));
    }

    @Test
    public void testCreateOnlyAtTheHostLevel() {
        assertNull(ftpMainStreamer.create(Paths.get("Network", "rebex", "pub"), true));
        assertTrue(ftpMainStreamer.findByPath(Paths.get("rebex", "pub")).isEmpty());
        assertTrue(ftpMainStreamer.findByPath(Paths.get("missing")).isEmpty());
    }
}
