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

import org.cosinus.streamer.api.remote.DefaultConnectionPool;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class SambaConnectionPool extends DefaultConnectionPool<SambaConnection, SambaFile> {

    public SambaConnectionPool(final SambaConnectionFactory sambaConnectionFactory,
                               final SambaConnectionPoolConfig sambaConnectionPoolConfig) {

        super(sambaConnectionFactory, sambaConnectionPoolConfig);
    }

    /**
     * Run a function on a borrowed connection, returning the connection to the pool afterward.
     *
     * @param key      the connection key
     * @param function the function to run
     * @param <V>      the type of the result
     * @return the function result
     */
    public <V> V withConnection(String key, Function<SambaConnection, V> function) {
        SambaConnection connection = borrowConnection(key);
        try {
            return function.apply(connection);
        } finally {
            returnConnection(key, connection);
        }
    }
}
