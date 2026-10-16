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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.network.UnknownNetworkHost;
import org.cosinus.streamer.samba.connection.TestNetworkConfigurationProvider;
import org.cosinus.streamer.samba.connection.TestDialogHandler;
import org.cosinus.streamer.samba.credentials.SambaCredentials;
import org.cosinus.streamer.samba.discovery.SambaDiscovery.DiscoveredHost;
import org.cosinus.streamer.samba.discovery.SambaHost;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;
import org.cosinus.streamer.samba.test.SambaTestApplication;
import org.cosinus.streamer.samba.test.TestSambaDiscovery;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.cosinus.streamer.samba.model.SambaFolderConfiguration.share;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.AFTER_METHOD;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaTestApplication.class)
public class SambaMainStreamerTest {

    @Autowired
    private SambaMainStreamer sambaMainStreamer;

    @Autowired
    private TestNetworkConfigurationProvider networkConfigurationProvider;

    @Autowired
    private TestSambaDiscovery sambaDiscovery;

    @Autowired
    private TestDialogHandler dialogHandler;

    @Test
    public void testHostName() {
        assertEquals("nas", sambaMainStreamer.hostName(Paths.get("nas")));
        assertEquals("nas", sambaMainStreamer.hostName(Paths.get("nas", "public", "dir", "file.txt")));
    }

    @Test
    public void testShareName() {
        assertEquals("public", sambaMainStreamer.shareName(Paths.get("nas", "public")));
        assertEquals("public", sambaMainStreamer.shareName(Paths.get("nas", "public", "dir", "file.txt")));
    }

    @Test
    public void testToStreamQuery() {
        assertEquals("", sambaMainStreamer.toInnerPath(Paths.get("nas")));
        assertEquals("", sambaMainStreamer.toInnerPath(Paths.get("nas", "public")));
        assertEquals("dir", sambaMainStreamer.toInnerPath(Paths.get("nas", "public", "dir")));
        assertEquals("dir/sub/file.txt", sambaMainStreamer.toInnerPath(Paths.get("nas", "public", "dir", "sub", "file.txt")));
    }

    @Test
    public void testNormalize() {
        assertEquals("", sambaMainStreamer.normalize(null));
        assertEquals("", sambaMainStreamer.normalize(""));
        assertEquals("", sambaMainStreamer.normalize("/"));
        assertEquals("a/b/c", sambaMainStreamer.normalize("a/b/c"));
        assertEquals("a/b/c", sambaMainStreamer.normalize("/a//b/c/"));
        assertEquals("a/b/c", sambaMainStreamer.normalize("\\a\\b\\\\c\\"));
        assertEquals("a/b/c", sambaMainStreamer.normalize("a\\b/c"));
    }

    @Test
    public void testResolve() {
        assertEquals("file.txt", sambaMainStreamer.resolve("", "file.txt"));
        assertEquals("file.txt", sambaMainStreamer.resolve(null, "file.txt"));
        assertEquals("dir/file.txt", sambaMainStreamer.resolve("dir", "file.txt"));
        assertEquals("dir/sub/file.txt", sambaMainStreamer.resolve("/dir/sub/", "file.txt"));
    }

    @Test
    public void testParentOfInnerFolderIsTheContainingFolder() {
        SambaStreamer<?> sub = sambaMainStreamer.createSambaStreamer(Paths.get("nas", "public", "dir", "sub"), true);

        Streamer<?> dir = sub.getParent();
        assertTrue(dir instanceof SambaParentStreamer);
        assertEquals(Paths.get("nas", "public", "dir"), dir.getPath());

        Streamer<?> share = dir.getParent();
        assertTrue(share instanceof SambaFolderStreamer);
        assertEquals(Paths.get("nas", "public"), share.getPath());

        assertTrue(share.getParent() instanceof SambaHostStreamer);
    }

    @Test
    public void testParentOfFileInShareIsTheShare() {
        SambaStreamer<?> file = sambaMainStreamer.createSambaStreamer(Paths.get("nas", "public", "file.txt"), false);

        assertTrue(file.getParent() instanceof SambaFolderStreamer);
        assertEquals(Paths.get("nas", "public"), file.getParent().getPath());
    }

    /*
     * The rename and refresh tests recreate the context after them, as the main streamer caches the samba hosts.
     */

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRenameHost() {
        givenHosts("nas", "office");
        SambaHostStreamer nas = sambaMainStreamer.createHostStreamer("nas");

        assertTrue(nas.rename(Paths.get("media")));

        Map<String, SambaConfiguration> saved = networkConfigurationProvider.getHosts(SambaConfiguration.class);
        assertEquals(Set.of("media", "office"), saved.keySet());
        assertEquals("media", saved.get("media").name());
        assertEquals("nas.lan", saved.get("media").host());
        assertEquals(List.of(share("public")), saved.get("media").folders());
        assertEquals(List.of(Paths.get("media"), Paths.get("office")),
            sambaMainStreamer.stream().map(Streamer::getPath).toList());
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRenameHostToUsedNameFails() {
        givenHosts("nas", "office");
        Map<String, SambaConfiguration> hosts = networkConfigurationProvider.getHosts(SambaConfiguration.class);

        assertFalse(sambaMainStreamer.createHostStreamer("nas").rename(Paths.get("office")));
        assertEquals(hosts, networkConfigurationProvider.getHosts(SambaConfiguration.class));
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRenameHostToInvalidPathFails() {
        givenHosts("nas");
        SambaHostStreamer nas = sambaMainStreamer.createHostStreamer("nas");
        Map<String, SambaConfiguration> hosts = networkConfigurationProvider.getHosts(SambaConfiguration.class);

        assertFalse(nas.rename(Paths.get("nas")));
        assertFalse(nas.rename(Paths.get("other", "media")));
        assertFalse(nas.rename(Paths.get("/tmp/media")));
        assertFalse(nas.rename(null));
        assertEquals(hosts, networkConfigurationProvider.getHosts(SambaConfiguration.class));
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRefreshKeepsConfiguredHosts() {
        givenHosts("nas");
        // load the hosts in cache, as when the network root is shown
        assertEquals(List.of(Paths.get("nas")), sambaMainStreamer.stream().map(Streamer::getPath).toList());
        sambaDiscovery.setDiscoveredHosts(List.of(new DiscoveredHost(new SambaHost("10.0.0.2", "office.lan"),
            new SambaConfiguration(null, "office.lan", 445, null, null, null, null, List.of(share("docs"))))));

        // as on refresh of the network root
        sambaMainStreamer.reset();
        sambaMainStreamer.discoverNetworkHosts();

        assertEquals(List.of(Paths.get("nas"), Paths.get("office")),
            sambaMainStreamer.stream().map(Streamer::getPath).toList());
        assertEquals(List.of("nas", "office"), List.copyOf(networkConfigurationProvider.getHosts(SambaConfiguration.class).keySet()));
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testDiscoveredHostAvoidsNamesOfOtherNetworkTypes() {
        givenHosts("nas");
        networkConfigurationProvider.saveHost(new UnknownNetworkHost("ftp", "office", "ftp.office.lan"));
        sambaDiscovery.setDiscoveredHosts(List.of(new DiscoveredHost(new SambaHost("10.0.0.2", "office.lan"),
            new SambaConfiguration(null, "office.lan", 445, null, null, null, null, List.of(share("docs"))))));

        List<SambaConfiguration> hosts = sambaMainStreamer.discoverNetworkHosts();

        assertEquals(List.of("nas", "office-2"), hosts.stream().map(SambaConfiguration::name).toList());
        assertEquals(List.of("nas", "office-2"), List.copyOf(networkConfigurationProvider.getHosts(SambaConfiguration.class).keySet()));
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRefreshFoldersReplacesFolders() {
        Map<String, SambaConfiguration> hosts = new LinkedHashMap<>();
        hosts.put("nas", new SambaConfiguration("nas", "nas.lan", null, null, "user", "secret", null,
            List.of(share("old"), new SambaFolderConfiguration("public", "media"))));
        hosts.put("office", new SambaConfiguration("office", "office.lan", null, null, null, null, null,
            List.of(share("docs"))));
        networkConfigurationProvider.setHosts(hosts);
        sambaDiscovery.setListedShares(List.of("photos", "PUBLIC"));

        assertTrue(sambaMainStreamer.refreshFolders("nas"));

        Map<String, SambaConfiguration> saved = networkConfigurationProvider.getHosts(SambaConfiguration.class);
        assertEquals(List.of("nas", "office"), List.copyOf(saved.keySet()));
        // the removed share is dropped, the kept one keeps its configuration, in the order listed by the server
        assertEquals(List.of(share("photos"), new SambaFolderConfiguration("public", "media")),
            saved.get("nas").folders());
        assertEquals("secret", saved.get("nas").password());
        assertEquals(hosts.get("office"), saved.get("office"));
        assertEquals("user", sambaDiscovery.getListedConfigurations().getFirst().username());
        assertEquals(saved.get("nas"), sambaMainStreamer.createHostStreamer("nas").getHostConfiguration());
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRefreshFoldersKeepsFoldersWhenListingFails() {
        givenHosts("nas");
        Map<String, SambaConfiguration> hosts = networkConfigurationProvider.getHosts(SambaConfiguration.class);
        sambaDiscovery.setListedShares(null);

        assertFalse(sambaMainStreamer.refreshFolders("nas"));
        assertEquals(hosts, networkConfigurationProvider.getHosts(SambaConfiguration.class));
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRefreshFoldersOfUnknownHostFails() {
        givenHosts("nas");
        sambaDiscovery.setListedShares(List.of("docs"));

        assertFalse(sambaMainStreamer.refreshFolders("office"));
        assertTrue(sambaDiscovery.getListedConfigurations().isEmpty());
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRefreshFoldersAsksCredentialsWhenGuestIsDisabled() {
        givenHosts("nas");
        sambaDiscovery.setListedShares(List.of("photos"));
        sambaDiscovery.setGuestDisabled(true);
        dialogHandler.reset(new SambaCredentials(null, "user", "secret"));

        assertTrue(sambaMainStreamer.refreshFolders("nas"));

        assertEquals(1, dialogHandler.getAskedUsernames().size());
        assertEquals(Arrays.asList(null, "user"), sambaDiscovery.getListedConfigurations()
            .stream()
            .map(SambaConfiguration::username)
            .toList());
        assertEquals(List.of(share("photos")),
            networkConfigurationProvider.getHosts(SambaConfiguration.class).get("nas").folders());
    }

    @Test
    @DirtiesContext(methodMode = AFTER_METHOD)
    public void testRefreshFoldersKeepsFoldersWhenCredentialsAreCancelled() {
        givenHosts("nas");
        Map<String, SambaConfiguration> hosts = networkConfigurationProvider.getHosts(SambaConfiguration.class);
        sambaDiscovery.setListedShares(List.of("photos"));
        sambaDiscovery.setGuestDisabled(true);
        dialogHandler.reset(null);

        assertFalse(sambaMainStreamer.refreshFolders("nas"));

        assertEquals(1, dialogHandler.getAskedUsernames().size());
        assertEquals(hosts, networkConfigurationProvider.getHosts(SambaConfiguration.class));
    }

    private void givenHosts(String... names) {
        Map<String, SambaConfiguration> hosts = new LinkedHashMap<>();
        for (String name : names) {
            hosts.put(name, new SambaConfiguration(name, name + ".lan", null, null, null, null, null,
                List.of(share("public"))));
        }
        networkConfigurationProvider.setHosts(hosts);
    }
}
