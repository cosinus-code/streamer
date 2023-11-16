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

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

public interface RemoteParentStreamer<S extends Streamer<?>, R, C extends Connection<R>>
    extends RemoteStreamer<S, R, C>, ParentStreamer<S> {

    @Override
    default Stream<S> stream() {
        return getConnection()
            .map(connection -> connection
                .stream(getStreamQuery())
                .map(this::createFromRemote)
                .onClose(() -> returnConnection(connection)))
            .orElseGet(Stream::empty);
    }

    @Override
    default Stream<S> flatStream(final FlatStreamingStrategy strategy, final StreamerFilter streamerFilter) {
        return getConnection()
            .map(connection -> StreamSupport
                .stream(new FlatStreamingSpliterator<>(strategy,
                    connection
                        .stream(getStreamQuery())
                        .map(this::createFromRemote)
                        .filter(streamerFilter),
                    streamer -> connection
                        .stream(((RemoteStreamer<S, R, C>) streamer).getStreamQuery())
                        .map(this::createFromRemote)), false)
                .onClose(() -> returnConnection(connection)))
            .orElseGet(Stream::empty);
    }

    @Override
    default void save() {
        getConnection()
            .ifPresent(connection -> {
                if (!connection.makeDirectory(getStreamQuery())) {
                    throw new SaveStreamerException("Failed to create ftp directory:" + getPath().toString());
                }
                returnConnection(connection);
            });
    }

    default Optional<C> getConnection() {
        return ofNullable(connectionPool())
            .flatMap(connectionPool -> ofNullable(connectionName())
                .map(connectionPool::borrowConnection));
    }

    default void returnConnection(C connection) {
        connectionPool().returnConnection(connectionName(), connection);
    }

    @Override
    default void execute(Path path) {
    }


    S createFromRemote(R remote);
}
