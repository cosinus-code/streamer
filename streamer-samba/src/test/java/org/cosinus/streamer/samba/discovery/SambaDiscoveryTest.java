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

import org.cosinus.streamer.samba.discovery.SambaDiscovery.DiscoveredHost;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cosinus.streamer.samba.model.SambaConfiguration.DEFAULT_PORT;
import static org.cosinus.streamer.samba.model.SambaFolderConfiguration.share;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaDiscoveryTestApplication.class)
public class SambaDiscoveryTest {

    @Autowired
    private SambaDiscovery sambaDiscovery;

    private static final SambaHost NAS = new SambaHost("192.168.1.10", "nas.fritz.box");

    @Test
    public void testNewHostIsAddedWithShortNameKey() {
        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(Map.of(),
            List.of(discovered(NAS, "user", "public", "media")));

        assertEquals(List.of("nas"), List.copyOf(merged.keySet()));
        SambaConfiguration nas = merged.get("nas");
        assertEquals("nas", nas.name());
        assertEquals("nas.fritz.box", nas.host());
        assertEquals("user", nas.username());
        assertEquals(List.of(share("public"), share("media")), nas.folders());
    }

    @Test
    public void testMissingSharesAreAppendedToExistingHost() {
        SambaConfiguration existingNas = host("my-nas", "192.168.1.10", "me", "Public");
        Map<String, SambaConfiguration> existing = Map.of("my-nas", existingNas);

        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(existing,
            List.of(discovered(NAS, null, "public", "media")));

        assertEquals(List.of("my-nas"), List.copyOf(merged.keySet()));
        SambaConfiguration nas = merged.get("my-nas");
        // the existing share is kept as is (case included) and the credentials are unchanged
        assertEquals(List.of(share("Public"), share("media")), nas.folders());
        assertEquals("me", nas.username());
        assertEquals("192.168.1.10", nas.host());
    }

    @Test
    public void testExistingHostIsMatchedByShortName() {
        Map<String, SambaConfiguration> existing = Map.of("nas", host("nas", "NAS", null, "public"));

        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(existing,
            List.of(discovered(NAS, null, "public")));

        assertEquals(existing, merged);
    }

    @Test
    public void testExistingHostsAndSharesAreKept() {
        SambaConfiguration home = host("home", "server", "me", "home", "backup");
        SambaConfiguration offline = host("offline", "192.168.1.99", null, "data");
        Map<String, SambaConfiguration> existing = new LinkedHashMap<>();
        existing.put("home", home);
        existing.put("offline", offline);

        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(existing,
            List.of(discovered(new SambaHost("192.168.1.5", "server"), null, "home")));

        assertEquals(List.of("home", "offline"), List.copyOf(merged.keySet()));
        assertEquals(home, merged.get("home"));
        assertEquals(offline, merged.get("offline"));
    }

    @Test
    public void testHostOnOtherPortIsAddedAsNewHost() {
        Map<String, SambaConfiguration> existing = Map.of("nas",
            new SambaConfiguration("nas", "nas", 1445, null, null, null, null, List.of(share("public"))));

        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(existing,
            List.of(discovered(NAS, null, "public")));

        assertEquals(List.of("nas", "nas-2"), List.copyOf(merged.keySet()));
        assertEquals(Integer.valueOf(DEFAULT_PORT), merged.get("nas-2").port());
    }

    @Test
    public void testKeyCollisionGetsSuffix() {
        Map<String, SambaConfiguration> merged = sambaDiscovery.merge(Map.of(), List.of(
            discovered(NAS, null, "public"),
            discovered(new SambaHost("10.0.0.10", "nas.office.lan"), null, "data")));

        assertEquals(List.of("nas", "nas-2"), List.copyOf(merged.keySet()));
        assertEquals("nas.office.lan", merged.get("nas-2").host());
        assertEquals("nas-2", merged.get("nas-2").name());
    }

    private static DiscoveredHost discovered(SambaHost host, String username, String... shares) {
        return new DiscoveredHost(host,
            new SambaConfiguration(null, host.name(), DEFAULT_PORT, null, username, null, null, shares(shares)));
    }

    private static SambaConfiguration host(String name, String host, String username, String... shares) {
        return new SambaConfiguration(name, host, null, null, username, null, null, shares(shares));
    }

    private static List<SambaFolderConfiguration> shares(String... shares) {
        return List.of(shares)
            .stream()
            .map(SambaFolderConfiguration::share)
            .toList();
    }
}
