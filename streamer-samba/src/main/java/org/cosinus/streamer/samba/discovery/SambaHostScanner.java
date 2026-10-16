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

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static java.net.NetworkInterface.getNetworkInterfaces;
import static java.nio.ByteBuffer.allocate;
import static java.util.Collections.list;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Finds the hosts of the local networks accepting samba connections,
 * by checking the samba port on all addresses of the local IPv4 subnets.
 */
@Component
@Slf4j
public class SambaHostScanner {

    /**
     * Subnets larger than this are reduced to the /24 subnet around the local address,
     * to keep the scan short.
     */
    public static final int MIN_PREFIX_LENGTH = 24;

    private static final int CONNECT_TIMEOUT_MILLIS = 500;

    /**
     * Some hosts take long to resolve (the reverse lookup is confirmed by a forward lookup),
     * so the ip address is used as name when the resolution takes longer.
     */
    private static final int RESOLVE_TIMEOUT_SECONDS = 2;

    private static final Executor VIRTUAL_THREAD_EXECUTOR = runnable -> Thread.ofVirtual().start(runnable);

    /**
     * Scan the local subnets for hosts accepting connections on a port.
     *
     * @param port the port to check
     * @return the found hosts
     */
    public List<SambaHost> scan(int port) {
        return scan(localSubnetAddresses(), port);
    }

    /**
     * Check a port on several addresses in parallel.
     *
     * @param addresses the addresses to check
     * @param port      the port to check
     * @return the hosts accepting connections on the port
     */
    public List<SambaHost> scan(Collection<InetAddress> addresses, int port) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            return addresses
                .stream()
                .map(address -> executor.submit(() -> isPortOpen(address, port) ?
                    Optional.of(address) :
                    Optional.<InetAddress>empty()))
                .map(this::getResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::resolveHost)
                .map(CompletableFuture::join)
                .toList();
        }
    }

    /**
     * Resolve the name of a host, using the ip address if the name is not resolved in time.
     * The resolution runs on its own virtual thread, so a slow lookup doesn't block the scan.
     *
     * @param address the host address
     * @return the host
     */
    protected CompletableFuture<SambaHost> resolveHost(InetAddress address) {
        String ip = address.getHostAddress();
        return CompletableFuture
            .supplyAsync(address::getCanonicalHostName, VIRTUAL_THREAD_EXECUTOR)
            .completeOnTimeout(ip, RESOLVE_TIMEOUT_SECONDS, SECONDS)
            .exceptionally(ex -> ip)
            .thenApply(name -> new SambaHost(ip, name));
    }

    protected boolean isPortOpen(InetAddress address, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT_MILLIS);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Get all host addresses of the subnets of the active local network interfaces.
     *
     * @return the addresses to scan
     */
    public Set<InetAddress> localSubnetAddresses() {
        Set<InetAddress> addresses = new LinkedHashSet<>();
        try {
            for (NetworkInterface networkInterface : list(getNetworkInterfaces())) {
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    networkInterface.getInterfaceAddresses()
                        .stream()
                        .filter(interfaceAddress -> interfaceAddress.getAddress() instanceof Inet4Address)
                        .forEach(interfaceAddress -> addresses.addAll(subnetHosts(
                            (Inet4Address) interfaceAddress.getAddress(),
                            interfaceAddress.getNetworkPrefixLength())));
                }
            }
        } catch (SocketException ex) {
            log.error("Failed to get the local network interfaces", ex);
        }
        return addresses;
    }

    /**
     * Get the host addresses in the subnet of an address, excluding the network and broadcast addresses.
     *
     * @param address      the address in the subnet
     * @param prefixLength the subnet prefix length; subnets larger than /24 are reduced to /24
     * @return the host addresses
     */
    public List<InetAddress> subnetHosts(Inet4Address address, int prefixLength) {
        int prefix = Math.max(prefixLength, MIN_PREFIX_LENGTH);
        if (prefix >= 31) {
            return List.of();
        }

        int mask = -1 << (32 - prefix);
        int network = ByteBuffer.wrap(address.getAddress()).getInt() & mask;
        int hostsCount = (1 << (32 - prefix)) - 2;

        return IntStream.rangeClosed(1, hostsCount)
            .mapToObj(host -> getAddress(network, host))
            .toList();
    }

    private InetAddress getAddress(int network, int host) {
        try {
            return InetAddress.getByAddress(
                allocate(4)
                    .putInt(network + host)
                    .array());
        } catch (UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private <T> Optional<T> getResult(Future<Optional<T>> future) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException ex) {
            return Optional.empty();
        }
    }
}
