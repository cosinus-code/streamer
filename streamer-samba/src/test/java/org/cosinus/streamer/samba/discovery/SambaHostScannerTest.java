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

import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = SambaDiscoveryTestApplication.class)
public class SambaHostScannerTest {

    @Autowired
    private SambaHostScanner sambaHostScanner;

    @Test
    public void testSubnetHosts() throws IOException {
        List<InetAddress> hosts = sambaHostScanner.subnetHosts(ipv4("192.168.1.42"), 24);

        assertEquals(254, hosts.size());
        assertEquals(ipv4("192.168.1.1"), hosts.getFirst());
        assertEquals(ipv4("192.168.1.254"), hosts.getLast());
    }

    @Test
    public void testSmallSubnetHosts() throws IOException {
        List<InetAddress> hosts = sambaHostScanner.subnetHosts(ipv4("10.0.0.5"), 30);

        assertEquals(List.of(ipv4("10.0.0.5"), ipv4("10.0.0.6")), hosts);
    }

    @Test
    public void testLargeSubnetIsReducedToSurroundingSlash24() throws IOException {
        List<InetAddress> hosts = sambaHostScanner.subnetHosts(ipv4("172.17.3.7"), 16);

        assertEquals(254, hosts.size());
        assertEquals(ipv4("172.17.3.1"), hosts.getFirst());
        assertEquals(ipv4("172.17.3.254"), hosts.getLast());
    }

    @Test
    public void testPointToPointSubnetHasNoHostsToScan() throws IOException {
        assertTrue(sambaHostScanner.subnetHosts(ipv4("10.0.0.1"), 32).isEmpty());
    }

    @Test
    public void testScanFindsOnlyOpenPorts() throws IOException {
        InetAddress localhost = InetAddress.getLoopbackAddress();

        try (ServerSocket serverSocket = new ServerSocket(0, 1, localhost)) {
            List<SambaHost> hosts = sambaHostScanner.scan(List.of(localhost), serverSocket.getLocalPort());

            assertEquals(1, hosts.size());
            assertEquals(localhost.getHostAddress(), hosts.getFirst().address());
        }

        int closedPort;
        try (ServerSocket serverSocket = new ServerSocket(0, 1, localhost)) {
            closedPort = serverSocket.getLocalPort();
        }
        assertTrue(sambaHostScanner.scan(List.of(localhost), closedPort).isEmpty());
    }

    private static Inet4Address ipv4(String address) throws IOException {
        return (Inet4Address) InetAddress.getByName(address);
    }
}
