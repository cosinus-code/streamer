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

import org.cosinus.streamer.api.BinaryStreamer;

import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Optional.ofNullable;

public interface RemoteBinaryStreamer<R, C extends Connection<R>>
    extends RemoteStreamer<byte[], R, C>, BinaryStreamer {

    @Override
    default InputStream inputStream() {
        String connectionKey = connectionName();
        ConnectionPool<C, R> connectionPool = connectionPool();
        if (connectionKey == null || connectionPool == null) {
            return null;
        }

        return ofNullable(connectionPool.borrowConnection(connectionKey))
            .map(connection -> connection.inputStream(getPath()))
            .orElse(null);
    }

    @Override
    default OutputStream outputStream(boolean append) {
        String connectionKey = connectionName();
        ConnectionPool<C, R> connectionPool = connectionPool();
        if (connectionKey == null || connectionPool == null) {
            return null;
        }

        return ofNullable(connectionPool.borrowConnection(connectionKey))
            .map(connection -> connection.outputStream(getPath(), append))
            .orElse(null);
    }
}
