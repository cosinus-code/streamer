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

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.cosinus.streamer.api.error.StreamerException;

/**
 * Default implementation of {@link ConnectionPool} using apache common objects pool.
 *
 * @param <C> the type of connection
 * @param <R> the type of remote objects streamed by connection
 */
public abstract class DefaultConnectionPool<C extends Connection<R>, R>
    extends GenericKeyedObjectPool<String, C>
    implements ConnectionPool<C, R> {

    public DefaultConnectionPool(final KeyedPooledObjectFactory<String, C> factory) {
        super(factory);
    }

    public DefaultConnectionPool(final KeyedPooledObjectFactory<String, C> factory,
                                 final GenericKeyedObjectPoolConfig<C> config) {
        super(factory, config);
    }

    public DefaultConnectionPool(final KeyedPooledObjectFactory<String, C> factory,
                                 final GenericKeyedObjectPoolConfig<C> config,
                                 final AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }

    @Override
    public C borrowConnection(String key) {
        try {
            return borrowObject(key);
        } catch (Exception e) {
            throw new StreamerException(e, "failed.to.borrow.connection.from.pool", key);
        }
    }

    @Override
    public void returnConnection(String key, C connection) {
        returnObject(key, connection);
    }
}
