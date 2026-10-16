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

package org.cosinus.streamer.samba.discovery;

import com.hierynomus.smbj.auth.AuthenticationContext;
import org.cosinus.streamer.samba.SambaTestServer;
import org.cosinus.streamer.samba.discovery.SambaDiscovery.DiscoveredHost;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.cosinus.streamer.samba.SambaTestServer.PASSWORD;
import static org.cosinus.streamer.samba.SambaTestServer.SHARE;
import static org.cosinus.streamer.samba.SambaTestServer.USERNAME;
import static org.cosinus.streamer.samba.model.SambaFolderConfiguration.share;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the samba shares discovery against a real samba server started in a docker container.
 * <p>
 * The tests are skipped when docker is not available.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaDiscoveryTestApplication.class)
public class SambaDiscoveryContainerTest {

    @Autowired
    private SambaShareLister sambaShareLister;

    @Autowired
    private SambaDiscovery sambaDiscovery;

    @BeforeClass
    public static void startSambaServer() {
        SambaTestServer.start();
    }

    @Test
    public void testListDiskShares() throws IOException {
        List<String> shares = sambaShareLister.listDiskShares(SambaTestServer.host(),
            SambaTestServer.port(),
            new AuthenticationContext(USERNAME, PASSWORD.toCharArray(), null));

        // the IPC$ share is not a disk share and must be filtered out
        assertEquals(List.of(SHARE), shares);
    }

    @Test
    public void testListSharesOfConfiguredHost() throws IOException {
        assertEquals(List.of(SHARE), sambaDiscovery.listShares(existingHost(PASSWORD)));
    }

    @Test
    public void testDiscoverSharesWithExistingCredentials() {
        SambaHost host = new SambaHost(SambaTestServer.host(), SambaTestServer.host());
        SambaConfiguration existing = existingHost(PASSWORD);

        List<DiscoveredHost> discovered = sambaDiscovery.discoverShares(List.of(host),
            SambaTestServer.port(),
            List.of(existing));

        assertEquals(1, discovered.size());
        SambaConfiguration configuration = discovered.getFirst().configuration();
        assertEquals(List.of(share(SHARE)), configuration.folders());
        assertEquals(Integer.valueOf(SambaTestServer.port()), configuration.port());
        assertEquals(USERNAME, configuration.username());
        assertEquals(PASSWORD, configuration.password());
        assertEquals("WORKGROUP", configuration.domain());
    }

    @Test
    public void testDiscoverSharesFallsBackToGuestWhenCredentialsAreWrong() {
        SambaHost host = new SambaHost(SambaTestServer.host(), SambaTestServer.host());
        SambaConfiguration existing = existingHost("wrong");

        List<DiscoveredHost> discovered = sambaDiscovery.discoverShares(List.of(host),
            SambaTestServer.port(),
            List.of(existing));

        // the shares are listed by the guest account, so they are discovered without credentials
        assertEquals(1, discovered.size());
        SambaConfiguration configuration = discovered.getFirst().configuration();
        assertEquals(List.of(share(SHARE)), configuration.folders());
        assertNull(configuration.username());
        assertNull(configuration.password());
    }

    @Test
    public void testDiscoveredSharesAreMergedIntoExistingHost() {
        SambaHost host = new SambaHost(SambaTestServer.host(), SambaTestServer.host());
        SambaConfiguration existing = existingHost(PASSWORD);

        List<DiscoveredHost> discovered = sambaDiscovery.discoverShares(List.of(host),
            SambaTestServer.port(),
            List.of(existing));
        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(Map.of("server", existing), discovered);

        assertEquals(List.of("server"), List.copyOf(merged.keySet()));
        assertEquals(List.of(share("other"), share(SHARE)), merged.get("server").folders());
        assertEquals(PASSWORD, merged.get("server").password());
    }

    private static SambaConfiguration existingHost(String password) {
        return new SambaConfiguration("server",
            SambaTestServer.host(),
            SambaTestServer.port(),
            "WORKGROUP",
            USERNAME,
            password,
            null,
            List.of(share("other")));
    }
}
