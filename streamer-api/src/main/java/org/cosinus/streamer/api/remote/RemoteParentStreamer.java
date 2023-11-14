/*
 * Copyright 2020 Cosinus Software
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
package org.cosinus.streamer.api.remote;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.streamer.api.stream.FlatStreamingSpliterator;
import org.cosinus.streamer.api.stream.FlatStreamingStrategy;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

public interface RemoteParentStreamer<S extends Streamer<?>, R, C extends Connection<R>>
    extends RemoteStreamer<S, R, C>, ParentStreamer<S> {

    @Override
    default Stream<S> stream() {
        String connectionName = connectionName();
        ConnectionPool<C, R> connectionPool = connectionPool();
        if (connectionName == null || connectionPool == null) {
            return Stream.empty();
        }

        return ofNullable(connectionPool.borrowConnection(connectionName))
            .map(connection -> connection
                .stream(getPath())
                .map(this::createFromRemote)
                .onClose(() -> connectionPool.returnConnection(connectionName, connection)))
            .orElseGet(Stream::empty);
    }

    @Override
    default Stream<S> flatStream(final FlatStreamingStrategy strategy, final StreamerFilter streamerFilter) {
        String connectionName = connectionName();
        ConnectionPool<C, R> connectionPool = connectionPool();
        if (connectionName == null || connectionPool == null) {
            return Stream.empty();
        }

        return ofNullable(connectionPool.borrowConnection(connectionName))
            .map(connection -> StreamSupport
                .stream(new FlatStreamingSpliterator<>(strategy,
                    connection
                        .stream(getPath())
                        .map(this::createFromRemote)
                        .filter(streamerFilter),
                    streamer -> connection
                        .stream(streamer.getPath())
                        .map(this::createFromRemote)), false)
                .onClose(() -> connectionPool.returnConnection(connectionName, connection)))
            .orElseGet(Stream::empty);
    }

    @Override
    default void save() {
        String connectionName = connectionName();
        ConnectionPool<C, R> connectionPool = connectionPool();
        if (connectionName == null || connectionPool == null) {
            return;
        }

        ofNullable(connectionPool.borrowConnection(connectionName()))
            .ifPresent(connection -> {
                if (!connection.makeDirectory(getPath())) {
                    throw new SaveStreamerException("Failed to create ftp directory:" + getPath().toString());
                }
                connectionPool.returnConnection(connectionName, connection);
            });
    }

    S createFromRemote(R remote);
}
