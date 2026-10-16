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


package org.cosinus.streamer.network;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * A type of network (e.g. samba, ftp), whose hosts are shown under the "Network" root streamer.
 * <p>
 * The implementations are annotated with {@link RootNetwork}, which gives their type.
 */
public interface NetworkStreamer<S extends NetworkHostStreamer<?>> extends ParentStreamer<S> {

    /**
     * @return the network type, from the {@link RootNetwork} annotation
     */
    default String getType() {
        return ofNullable(findMergedAnnotation(getClass(), RootNetwork.class))
            .map(RootNetwork::name)
            .orElseGet(() -> getClass().getSimpleName());
    }

    /**
     * Set by the "Network" root streamer, as the parent of the hosts.
     *
     * @param parent the parent of the hosts
     */
    void setParent(ParentStreamer<?> parent);

    /**
     * Search the hosts in the local networks and save the new ones in network.json.
     * <p>
     * It is called on refresh, by the background loading worker.
     *
     * @return the configured hosts, including the discovered ones
     */
    List<? extends NetworkHost> discoverNetworkHosts();

    /**
     * @param hostName the host name
     * @return true if the host is configured
     */
    boolean existHost(String hostName);

    /**
     * Find a streamer by its path, as in its url, without the protocol.
     *
     * @param path the path, starting with the host name
     * @return the streamer, or empty if not found
     */
    Optional<S> findByPath(Path path);
}
