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

import com.hierynomus.mssmb2.SMBApiException;
import org.cosinus.streamer.samba.SambaParentStreamer;
import org.cosinus.streamer.samba.SambaTestServer;
import org.cosinus.streamer.samba.credentials.SambaCredentials;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.swing.error.AbortActionException;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.cosinus.streamer.samba.SambaTestServer.PASSWORD;
import static org.cosinus.streamer.samba.SambaTestServer.SHARE;
import static org.cosinus.streamer.samba.SambaTestServer.USERNAME;
import static org.cosinus.streamer.samba.model.SambaFolderConfiguration.share;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests asking the credentials when the access to a samba share is denied,
 * against a real samba server started in a docker container.
 * <p>
 * The tests are skipped when docker is not available.
 * <p>
 * Each test uses its own host name, as the connection factory keeps the entered credentials by host name.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaConnectionTestApplication.class)
public class SambaConnectionCredentialsContainerTest {

    @Rule
    public TestName testName = new TestName();

    @Autowired
    private SambaConnectionFactory sambaConnectionFactory;

    @Autowired
    private SambaConnectionPool sambaConnectionPool;

    @Autowired
    private TestNetworkConfigurationProvider networkConfigurationProvider;

    @Autowired
    private TestDialogHandler dialogHandler;

    @Before
    public void startServer() {
        SambaTestServer.start();
    }

    @Test
    public void testGuestAccessDeniedAsksCredentials() throws Exception {
        SambaConnectionFactory factory = factory(null, null, new SambaCredentials(null, USERNAME, PASSWORD));

        assertConnects(factory);
        assertEquals(1, dialogHandler.getAskedUsernames().size());
    }

    @Test
    public void testWrongPasswordAsksCredentialsWithUsername() throws Exception {
        SambaConnectionFactory factory = factory(USERNAME, "wrong", new SambaCredentials(null, USERNAME, PASSWORD));

        assertConnects(factory);
        assertEquals(List.of(USERNAME), dialogHandler.getAskedUsernames());
    }

    @Test
    public void testEnteredCredentialsAreReused() throws Exception {
        SambaConnectionFactory factory = factory(null, null, new SambaCredentials(null, USERNAME, PASSWORD));

        assertConnects(factory);
        assertConnects(factory);
        assertEquals(1, dialogHandler.getAskedUsernames().size());
    }

    @Test
    public void testCancelledCredentialsAbortsTheConnection() {
        SambaConnectionFactory factory = factory(null, null, null);

        AbortActionException ex = assertThrows(AbortActionException.class, () -> factory.create(connectionKey()));
        assertTrue(ex.getCause() instanceof SMBApiException);
        assertEquals(1, dialogHandler.getAskedUsernames().size());
    }

    @Test
    public void testCancelledCredentialsAbortsTheBorrowFromPool() {
        factory(null, null, null);

        // not wrapped by the pool, so the worker aborts the action without showing an error
        assertThrows(AbortActionException.class, () -> sambaConnectionPool.borrowConnection(connectionKey()));
    }

    @Test
    public void testCancelledCredentialsAreAskedOnceByStreamer() {
        factory(null, null, null);
        SambaParentStreamer streamer = new SambaParentStreamer(Paths.get(hostName(), SHARE), null);

        assertThrows(AbortActionException.class, streamer::stream);
        // the free space is shown after the loading is aborted, without asking the credentials again
        assertEquals(0, streamer.getFreeSpace());
        assertEquals(0, streamer.getTotalSpace());
        assertEquals(1, dialogHandler.getAskedUsernames().size());

        streamer.reset();
        assertThrows(AbortActionException.class, streamer::stream);
        assertEquals(2, dialogHandler.getAskedUsernames().size());
    }

    private void assertConnects(SambaConnectionFactory factory) throws Exception {
        SambaConnection connection = factory.create(connectionKey());
        try {
            assertTrue(connection.isConnected());
        } finally {
            connection.disconnect();
        }
    }

    private String hostName() {
        return testName.getMethodName();
    }

    private String connectionKey() {
        return sambaConnectionFactory.connectionKey(hostName(), SHARE);
    }

    private SambaConnectionFactory factory(String username, String password, SambaCredentials enteredCredentials) {
        SambaConfiguration hostConfiguration = new SambaConfiguration(hostName(),
            SambaTestServer.host(),
            SambaTestServer.port(),
            null,
            username,
            password,
            10,
            List.of(share(SHARE)));
        networkConfigurationProvider.setHosts(Map.of(hostName(), hostConfiguration));
        dialogHandler.reset(enteredCredentials);
        return sambaConnectionFactory;
    }
}
