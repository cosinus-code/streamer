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

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import static java.util.Optional.ofNullable;

public abstract class AbstractConnectionFactory<K, C extends Connection<R>, R>
    extends BaseKeyedPooledObjectFactory<K, C> {

    @Override
    public PooledObject<C> wrap(C connection) {
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public boolean validateObject(K key, PooledObject<C> pooledConnection) {
        return ofNullable(pooledConnection)
            .map(PooledObject::getObject)
            .map(this::validateConnection)
            .orElse(false);
    }

    @Override
    public void destroyObject(K key, PooledObject<C> pooledConnection) {
        ofNullable(pooledConnection)
            .map(PooledObject::getObject)
            .ifPresent(this::destroyConnection);
    }

    public abstract boolean validateConnection(C connection);

    public abstract void destroyConnection(C connection);
}
