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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.network.TestNetworkStreamer.TestHostStreamer;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.cosinus.streamer.network.NetworkMainStreamer.NETWORK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NetworkMainStreamerTest {

    private TestNetworkStreamer samba;

    private TestNetworkStreamer ftp;

    private NetworkMainStreamer networkMainStreamer;

    @Before
    public void createNetworkMainStreamer() {
        samba = new TestNetworkStreamer("Samba", "smb://", "nas", "office");
        ftp = new TestNetworkStreamer("Ftp", "ftp://", "rebex");
        networkMainStreamer = new NetworkMainStreamer(List.of(samba, ftp));
        networkMainStreamer.setName(NETWORK);
    }

    @Test
    public void testNetworkStreamersHaveTheNetworkRootAsParent() {
        assertSame(networkMainStreamer, samba.getParent());
        assertSame(networkMainStreamer, ftp.getParent());
    }

    @Test
    public void testStreamHostsOfAllTypesSortedByName() {
        assertEquals(List.of(Paths.get("nas"), Paths.get("office"), Paths.get("rebex")),
            networkMainStreamer.stream().map(Streamer::getPath).toList());
        assertEquals(List.of("smb://", "smb://", "ftp://"),
            networkMainStreamer.stream().map(Streamer::getProtocol).toList());
    }

    @Test
    public void testRefreshDiscoversHostsOnceOnTheNextStream() {
        networkMainStreamer.stream().toList();
        assertEquals(0, samba.getDiscoveries());

        networkMainStreamer.reset();
        assertEquals(1, samba.getResets());
        assertEquals(1, ftp.getResets());
        assertEquals(0, samba.getDiscoveries());

        networkMainStreamer.stream().toList();
        networkMainStreamer.stream().toList();
        assertEquals(1, samba.getDiscoveries());
        assertEquals(1, ftp.getDiscoveries());
    }

    @Test
    public void testUrlsAreRoutedByProtocol() {
        assertTrue(networkMainStreamer.isCompatible("smb://nas/public"));
        assertTrue(networkMainStreamer.isCompatible("ftp://rebex"));
        assertTrue(networkMainStreamer.isCompatible("network://Network"));
        assertFalse(networkMainStreamer.isCompatible("file:///tmp"));

        assertEquals(Optional.of(samba.host("nas/public")), networkMainStreamer.findByUrlPath("smb://nas/public"));
        assertEquals(Optional.of(ftp.host("rebex")), networkMainStreamer.findByUrlPath("ftp://rebex"));
        assertEquals(Optional.of(networkMainStreamer), networkMainStreamer.findByUrlPath("network://Network"));
        // an unknown host of a known protocol opens the network root
        assertEquals(Optional.of(networkMainStreamer), networkMainStreamer.findByUrlPath("smb://unknown"));
    }

    @Test
    public void testCreateFtpHost() {
        Streamer<?> created = create("ftp://server");

        assertTrue(created instanceof TestHostStreamer host && host.protocol().equals("ftp://"));
        assertEquals(List.of(Paths.get(NETWORK, "server")), ftp.getCreatedPaths());
        assertTrue(samba.getCreatedPaths().isEmpty());
    }

    @Test
    public void testCreateSambaHost() {
        Streamer<?> created = create("smb://server");

        assertTrue(created instanceof TestHostStreamer host && host.protocol().equals("smb://"));
        assertEquals(List.of(Paths.get(NETWORK, "server")), samba.getCreatedPaths());
        assertTrue(ftp.getCreatedPaths().isEmpty());
    }

    @Test
    public void testCreateSambaHostWithShare() {
        create("smb://server/photo");

        assertEquals(List.of(Paths.get(NETWORK, "server", "photo")), samba.getCreatedPaths());
    }

    @Test
    public void testCreateSambaHostWithoutProtocol() {
        create("//server");
        create("//server/photo");

        assertEquals(List.of(Paths.get(NETWORK, "server"), Paths.get(NETWORK, "server", "photo")),
            samba.getCreatedPaths());
        assertTrue(ftp.getCreatedPaths().isEmpty());
    }

    @Test
    public void testCreateWithoutProtocolOrHost() {
        assertNull(networkMainStreamer.create(Paths.get(NETWORK), true));
        assertNull(create("server"));
        assertNull(create("smb://"));
        assertNull(create("//"));
        assertNull(create("http://server"));
        assertTrue(samba.getCreatedPaths().isEmpty());
        assertTrue(ftp.getCreatedPaths().isEmpty());
    }

    /**
     * Create as the create action does, by resolving the entered location against the root path.
     */
    private Streamer<?> create(String location) {
        return networkMainStreamer.create(networkMainStreamer.getPath().resolve(location), true);
    }

    @Test
    public void testTypeFromRootNetworkAnnotation() {
        assertEquals("Annotated", new AnnotatedNetworkStreamer().getType());
    }

    @RootNetwork("Annotated")
    private static class AnnotatedNetworkStreamer extends TestNetworkStreamer {

        AnnotatedNetworkStreamer() {
            super(null, "test://");
        }
    }
}
