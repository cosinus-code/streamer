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

import org.cosinus.streamer.api.Streamer;

import java.util.Optional;

import static java.util.Optional.ofNullable;

public interface RemoteStreamer<T, R, C extends Connection<R>> extends Streamer<T> {

    default String connectionName() {
        return getName();
    }

    default Optional<C> getConnection() {
        return ofNullable(connectionPool())
            .flatMap(connectionPool -> ofNullable(connectionName())
                .map(connectionPool::borrowConnection));
    }

    default void returnConnection(C connection) {
        connectionPool().returnConnection(connectionName(), connection);
    }

    ConnectionPool<C, R> connectionPool();

    String getStreamQuery();
}
