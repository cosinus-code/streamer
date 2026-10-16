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

import org.cosinus.streamer.samba.SambaMainStreamer;
import org.cosinus.streamer.samba.SambaTestServer;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.samba.SambaTestServer.HELLO_CONTENT;
import static org.cosinus.streamer.samba.SambaTestServer.PASSWORD;
import static org.cosinus.streamer.samba.SambaTestServer.SHARE;
import static org.cosinus.streamer.samba.SambaTestServer.USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link SambaConnection} against a real samba server started in a docker container.
 * <p>
 * The tests are skipped when docker is not available.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaConnectionTestApplication.class)
public class SambaConnectionContainerTest {

    private static final String HOST_NAME = "test";

    @Autowired
    private SambaMainStreamer sambaMainStreamer;

    @Autowired
    private SambaConnectionPool sambaConnectionPool;

    @Autowired
    private SambaConnectionFactory sambaConnectionFactory;

    @Autowired
    private TestNetworkConfigurationProvider networkConfigurationProvider;

    private String connectionKey;

    private SambaConnection connection;

    @BeforeClass
    public static void startSambaServer() {
        SambaTestServer.start();
    }

    @Before
    public void borrowConnection() {
        networkConfigurationProvider.setHosts(Map.of(HOST_NAME, new SambaConfiguration(HOST_NAME,
            SambaTestServer.host(),
            SambaTestServer.port(),
            null,
            USERNAME,
            PASSWORD,
            10,
            List.of(SambaFolderConfiguration.share(SHARE)))));
        connectionKey = sambaConnectionFactory.connectionKey(HOST_NAME, SHARE);

        connection = sambaConnectionPool.borrowConnection(connectionKey);
        connection.mkdirs("work");
    }

    @After
    public void returnConnection() {
        connection.info("work")
            .ifPresent(work -> connection.delete(work, false));
        sambaConnectionPool.returnConnection(connectionKey, connection);
    }

    @Test
    public void testConnection() {
        assertTrue(connection.isConnected());
        assertTrue(connection.shareInfo().getTotalSpace() > 0);
    }

    @Test
    public void testList() {
        List<SambaFile> files = connection.stream("").toList();

        assertTrue(files.stream().anyMatch(file -> file.directory() && file.name().equals("sub")));
        assertTrue(files.stream().noneMatch(file -> file.name().equals(".") || file.name().equals("..")));
        assertTrue(connection.stream("sub")
            .anyMatch(file -> !file.directory() && file.innerPath().equals("sub/hello.txt")));
    }

    @Test
    public void testInfo() {
        SambaFile hello = connection.info("sub/hello.txt").orElseThrow();

        assertFalse(hello.directory());
        assertEquals(HELLO_CONTENT.length(), hello.size());
        assertTrue(hello.lastModified() > 0);
        assertTrue(connection.info("sub").orElseThrow().directory());
        assertTrue(connection.info("").orElseThrow().directory());
    }

    @Test
    public void testInfoMissing() {
        assertTrue(connection.info("missing.txt").isEmpty());
        assertTrue(connection.info("missing/dir/file.txt").isEmpty());
        assertFalse(connection.exists("missing.txt"));
    }

    @Test
    public void testRead() throws IOException {
        try (InputStream inputStream = connection.inputStream("sub/hello.txt")) {
            assertEquals(HELLO_CONTENT, new String(inputStream.readAllBytes(), UTF_8));
        }
    }

    @Test
    public void testMkdirs() {
        connection.mkdirs("work/a/b/c");

        assertTrue(connection.info("work/a/b/c").orElseThrow().directory());
    }

    @Test
    public void testSaveDirectory() {
        SambaFile saved = connection.save(SambaFile.sambaFolder("work/dir"));

        assertTrue(saved.directory());
        assertEquals("work/dir", saved.innerPath());
    }

    @Test
    public void testSaveEmptyFile() {
        SambaFile saved = connection.save(SambaFile.sambaFile("work/empty.txt"));

        assertFalse(saved.directory());
        assertEquals(0, saved.size());
    }

    @Test
    public void testWriteAppendAndOverwrite() throws IOException {
        write("work/file.txt", "12345", false);
        write("work/file.txt", "678", true);
        assertEquals("12345678", read("work/file.txt"));

        write("work/file.txt", "xy", false);
        assertEquals("xy", read("work/file.txt"));
        assertEquals(2, connection.info("work/file.txt").orElseThrow().size());
    }

    @Test
    public void testRename() throws IOException {
        write("work/old.txt", "content", false);
        connection.mkdirs("work/target");

        connection.rename("work/old.txt", "work/target/new.txt");

        assertFalse(connection.exists("work/old.txt"));
        assertEquals("content", read("work/target/new.txt"));
    }

    @Test
    public void testDeleteFile() throws IOException {
        write("work/file.txt", "content", false);

        assertTrue(connection.delete(connection.info("work/file.txt").orElseThrow(), false));
        assertFalse(connection.exists("work/file.txt"));
    }

    @Test
    public void testDeleteDirectoryRecursively() throws IOException {
        connection.mkdirs("work/dir/sub");
        write("work/dir/sub/file.txt", "content", false);

        assertTrue(connection.delete(connection.info("work/dir").orElseThrow(), false));
        assertFalse(connection.exists("work/dir"));
    }

    @Test
    public void testPoolReturnsConnection() {
        int active = sambaConnectionPool.getNumActive(connectionKey);

        boolean exists = sambaConnectionPool.withConnection(connectionKey,
            pooledConnection -> pooledConnection.exists("sub/hello.txt"));

        assertTrue(exists);
        assertEquals(active, sambaConnectionPool.getNumActive(connectionKey));
    }

    private void write(String query, String content, boolean append) throws IOException {
        try (OutputStream outputStream = connection.outputStream(null, query, append)) {
            outputStream.write(content.getBytes(UTF_8));
        }
    }

    private String read(String query) throws IOException {
        try (InputStream inputStream = connection.inputStream(query)) {
            return new String(inputStream.readAllBytes(), UTF_8);
        }
    }
}
