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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.samba.connection.SambaConnectionFactory;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static com.hierynomus.smbj.auth.AuthenticationContext.anonymous;
import static com.hierynomus.smbj.auth.AuthenticationContext.guest;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cosinus.streamer.samba.model.SambaConfiguration.DEFAULT_PORT;

/**
 * Discovers the samba shared folders available in the local networks.
 */
@Component
public class SambaDiscovery {

    private static final Logger LOG = LogManager.getLogger(SambaDiscovery.class);

    private final SambaHostScanner sambaHostScanner;

    private final SambaShareLister sambaShareLister;

    private final SambaConnectionFactory sambaConnectionFactory;

    public SambaDiscovery(final SambaHostScanner sambaHostScanner,
                          final SambaShareLister sambaShareLister,
                          final SambaConnectionFactory sambaConnectionFactory) {
        this.sambaHostScanner = sambaHostScanner;
        this.sambaShareLister = sambaShareLister;
        this.sambaConnectionFactory = sambaConnectionFactory;
    }

    /**
     * Search the shared folders in the local networks and add the new ones to the existing samba hosts.
     *
     * @param existing the existing samba hosts
     * @return the existing samba hosts, with the newly discovered shares and hosts
     */
    public Map<String, SambaConfiguration> discover(Map<String, SambaConfiguration> existing) {
        List<SambaHost> hosts = sambaHostScanner.scan(DEFAULT_PORT);
        return merge(existing, discoverShares(hosts, DEFAULT_PORT, existing.values()));
    }

    /**
     * List the shared folders of a configured samba host, using its credentials.
     *
     * @param configuration the samba host configuration
     * @return the names of the shared folders
     * @throws IOException if the connection or the share listing fails
     */
    public List<String> listShares(SambaConfiguration configuration) throws IOException {
        return sambaShareLister.listDiskShares(configuration.host(),
            configuration.portOrDefault(),
            sambaConnectionFactory.createAuthenticationContext(configuration));
    }

    /**
     * List in parallel the shared folders of several hosts.
     *
     * @param hosts    the hosts
     * @param port     the samba port
     * @param existing the existing samba hosts, whose credentials are tried for the matching hosts
     * @return the hosts with their discovered shares
     */
    public List<DiscoveredHost> discoverShares(List<SambaHost> hosts,
                                               int port,
                                               Collection<SambaConfiguration> existing) {
        try (ExecutorService executor = newVirtualThreadPerTaskExecutor()) {
            return hosts.stream()
                .map(host -> executor.submit(() -> discoverShares(host, port, existing)))
                .<Optional<DiscoveredHost>>map(result -> {
                    try {
                        return result.get();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        LOG.warn("Failed to discover samba shares", ex.getCause());
                    }
                    return empty();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        }
    }

    /**
     * List the shared folders of a host, trying first the credentials of the existing configuration of that host,
     * then the guest and the anonymous accounts.
     *
     * @param host     the host
     * @param port     the samba port
     * @param existing the existing samba hosts
     * @return the host with its discovered shares, or empty if the shares cannot be listed
     */
    protected Optional<DiscoveredHost> discoverShares(SambaHost host,
                                                      int port,
                                                      Collection<SambaConfiguration> existing) {
        List<SambaConfiguration> credentials = concat(
            existing.stream()
                .filter(configuration -> host.matches(configuration.host()))
                .filter(configuration -> !isBlank(configuration.username())),
            Stream.of(guestCredentials(host, port)))
            .map(configuration -> new SambaConfiguration(null,
                host.name(),
                port == DEFAULT_PORT ? null : port,
                configuration.domain(),
                configuration.username(),
                configuration.password(),
                null,
                null))
            .distinct()
            .toList();

        for (SambaConfiguration credential : credentials) {
            for (AuthenticationContext authenticationContext : authenticationContexts(credential)) {
                try {
                    List<SambaFolderConfiguration> shares = sambaShareLister
                        .listDiskShares(host.address(), port, authenticationContext)
                        .stream()
                        .map(SambaFolderConfiguration::share)
                        .toList();
                    LOG.info("Found {} samba shares on {}", shares.size(), host.name());
                    return Optional.of(new DiscoveredHost(host, credential.withFolders(shares)));
                } catch (Exception ex) {
                    LOG.debug("Failed to list samba shares on {} as {}",
                        host.name(), authenticationContext.getUsername(), ex);
                }
            }
        }

        LOG.info("Cannot list samba shares on {}: access denied", host.name());
        return empty();
    }

    private SambaConfiguration guestCredentials(SambaHost host, int port) {
        return new SambaConfiguration(null, host.name(), port, null, null, null, null, null);
    }

    private List<AuthenticationContext> authenticationContexts(SambaConfiguration credential) {
        return isBlank(credential.username()) ?
            asList(guest(), anonymous()) :
            List.of(sambaConnectionFactory.createAuthenticationContext(credential));
    }

    /**
     * Add the discovered shares to the existing samba hosts.
     * <ul>
     * <li>The shares of an already configured host are appended to its shares list,
     * keeping its credentials unchanged.</li>
     * <li>A new host is added with the key of its short name, also used as its name,
     * and the credentials used to list its shares. If the key is already used, a "-2", "-3"... suffix is added.</li>
     * <li>The existing hosts and shares are never removed, even if they were not discovered,
     * since the hosts may be temporarily offline or not visible from this network.</li>
     * </ul>
     *
     * @param existing        the existing samba hosts
     * @param discoveredHosts the discovered hosts with their shares
     * @return the merged samba hosts
     */
    public Map<String, SambaConfiguration> merge(Map<String, SambaConfiguration> existing,
                                                 List<DiscoveredHost> discoveredHosts) {
        Map<String, SambaConfiguration> merged = new LinkedHashMap<>(existing);
        for (DiscoveredHost discoveredHost : discoveredHosts) {
            existing.entrySet()
                .stream()
                .filter(entry -> discoveredHost.isConfiguredBy(entry.getValue()))
                .findFirst()
                .ifPresentOrElse(
                    entry -> merged.put(
                        entry.getKey(),
                        addMissingShares(merged.get(entry.getKey()), discoveredHost.configuration().folders())),
                    () -> {
                        String key = uniqueKey(merged, discoveredHost.host().shortName());
                        merged.put(key, discoveredHost.configuration().withName(key));
                    });
        }
        return merged;
    }

    private static String uniqueKey(Map<String, SambaConfiguration> configurations, String key) {
        String uniqueKey = key;
        for (int index = 2; configurations.containsKey(uniqueKey); index++) {
            uniqueKey = key + "-" + index;
        }
        return uniqueKey;
    }

    private SambaConfiguration addMissingShares(SambaConfiguration configuration,
                                                List<SambaFolderConfiguration> shares) {
        List<SambaFolderConfiguration> missingShares = shares.stream()
            .filter(share -> configuration.findShare(share.name()).isEmpty())
            .toList();
        return missingShares.isEmpty() ?
            configuration :
            configuration.withFolders(
                concat(configuration.folders().stream(), missingShares.stream()).toList());
    }

    /**
     * A samba host found in the network, with its shared folders.
     *
     * @param host          the host
     * @param configuration the samba host configuration, with the credentials used to list the shares
     */
    public record DiscoveredHost(SambaHost host, SambaConfiguration configuration) {

        /**
         * Check if an existing samba host configuration points to this host.
         *
         * @param existing the existing samba host configuration
         * @return true if the host and the port are the same
         */
        public boolean isConfiguredBy(SambaConfiguration existing) {
            return host.matches(existing.host()) &&
                existing.portOrDefault() == configuration.portOrDefault();
        }
    }
}
