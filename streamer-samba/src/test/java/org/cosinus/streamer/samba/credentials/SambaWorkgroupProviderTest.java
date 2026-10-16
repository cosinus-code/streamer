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


package org.cosinus.streamer.samba.credentials;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class SambaWorkgroupProviderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testWorkgroupFromUserDomain() throws IOException {
        SambaWorkgroupProvider provider = new SambaWorkgroupProvider(Map.of("USERDOMAIN", "OFFICE"),
            sambaConfig("[global]\n   workgroup = WORKGROUP\n"));

        assertEquals(Optional.of("OFFICE"), provider.currentWorkgroup());
    }

    @Test
    public void testWorkgroupFromSambaConfig() throws IOException {
        SambaWorkgroupProvider provider = new SambaWorkgroupProvider(Map.of(),
            sambaConfig("""
                # workgroup = COMMENTED
                [homes]
                   workgroup = HOMES
                [Global]
                ;  workgroup = COMMENTED
                   Workgroup = HOME
                   server string = %h server
                """));

        assertEquals(Optional.of("HOME"), provider.currentWorkgroup());
    }

    @Test
    public void testNoWorkgroup() throws IOException {
        assertEquals(Optional.empty(), new SambaWorkgroupProvider(Map.of("USERDOMAIN", " "),
            sambaConfig("[global]\n   server string = %h server\n")).currentWorkgroup());
        assertEquals(Optional.empty(), new SambaWorkgroupProvider(Map.of(),
            temporaryFolder.getRoot().toPath().resolve("missing.conf")).currentWorkgroup());
    }

    private Path sambaConfig(String content) throws IOException {
        Path sambaConfig = temporaryFolder.newFile("smb.conf").toPath();
        Files.writeString(sambaConfig, content);
        return sambaConfig;
    }
}
