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

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration for {@link SambaConnection} pool.
 * <p>
 * Opening a samba connection is expensive (negotiation and authentication),
 * so idle connections are kept in the pool for a while and validated on borrow.
 */
@Component
public class SambaConnectionPoolConfig extends GenericKeyedObjectPoolConfig<SambaConnection> {

    public SambaConnectionPoolConfig() {
        setTestOnBorrow(true);
        setMinEvictableIdleDuration(Duration.ofMinutes(5));
        setTimeBetweenEvictionRuns(Duration.ofMinutes(1));
    }
}
