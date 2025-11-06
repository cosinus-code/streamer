/*
 * Copyright 2025 Cosinus Software
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

import org.cosinus.stream.StreamingStrategy;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.stream.FlatStreamingSpliterator;
import org.cosinus.stream.FlatStreamingStrategy;
import org.cosinus.stream.StreamSupplier;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RemoteParentStreamer<S extends Streamer<?>, R, C extends Connection<R>>
    extends RemoteStreamer<S, R, C>, ParentStreamer<S> {

    @Override
    default Stream<S> stream() {
        return streamFromRemote(connection -> connection
            .stream(getStreamQuery()))
            .map(this::createFromRemote);
    }

    @Override
    default Stream<S> flatStream(final FlatStreamingStrategy flatStreamingStrategy,
                                 final StreamingStrategy streamingStrategy,
                                 final StreamerFilter streamerFilter) {
        return getConnection()
            .map(connection -> StreamSupport
                .stream(new FlatStreamingSpliterator<>(
                    flatStreamingStrategy,
                    streamingStrategy,
                    streamFromConnection(connection).filter(streamerFilter),
                    streamSupplier(connection)), false)
                .onClose(() -> returnConnection(connection)))
            .orElseGet(Stream::empty);
    }

    private Stream<S> streamFromConnection(final C connection) {
        return connection
            .stream(getStreamQuery())
            .map(this::createFromRemote);
    }

    private Function<StreamSupplier<S>, Stream<S>> streamSupplier(final C connection) {
        return streamer -> {
            RemoteParentStreamer<S, R, C> remoteStreamer = (RemoteParentStreamer<S, R, C>) streamer;
            return connection
                .stream(remoteStreamer.getStreamQuery())
                .map(remoteStreamer::createFromRemote);
        };
    }

    @Override
    default void save() {
        runRemote(connection -> {
            if (connection.save(getRemote()) == null) {
                throw new SaveStreamerException("Failed to save streamer:" + getPath().toString());
            }
        });
    }

    S createFromRemote(R remote);
}
