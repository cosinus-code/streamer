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

import com.hierynomus.protocol.transport.TransportException;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.ConnectException;
import java.net.NoRouteToHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaConnectionTestApplication.class)
public class SambaConnectionFactoryTest {

    @Autowired
    private SambaConnectionFactory sambaConnectionFactory;

    @Test
    public void testConnectionKey() {
        String key = sambaConnectionFactory.connectionKey("nas", "public");

        assertEquals("nas/public", key);
        assertEquals("nas", sambaConnectionFactory.hostName(key));
        assertEquals("public", sambaConnectionFactory.shareName(key));
    }

    @Test
    public void testConnectionKeyWithSpecialCharacters() {
        String key = sambaConnectionFactory.connectionKey("public@nas-2", "filme 2000");

        assertEquals("public@nas-2", sambaConnectionFactory.hostName(key));
        assertEquals("filme 2000", sambaConnectionFactory.shareName(key));
    }

    @Test
    public void testIsNoRouteToHost() {
        assertTrue(sambaConnectionFactory.isNoRouteToHost(new NoRouteToHostException("No route to host")));
        assertTrue(sambaConnectionFactory.isNoRouteToHost(
            new TransportException(new NoRouteToHostException("No route to host"))));
        assertFalse(sambaConnectionFactory.isNoRouteToHost(new ConnectException("Connection refused")));
    }

    @Test
    public void testKeyWithoutShare() {
        assertEquals("nas", sambaConnectionFactory.hostName("nas"));
        assertEquals("", sambaConnectionFactory.shareName("nas"));
    }
}
