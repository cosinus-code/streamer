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

package org.cosinus.streamer.google.drive.connection;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.cosinus.streamer.google.drive.GoogleDriveComponent;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration for {@link GoogleDriveConnection}
 */
@GoogleDriveComponent
public class GoogleDriveConnectionPoolConfig extends GenericKeyedObjectPoolConfig<GoogleDriveConnection> {

    public GoogleDriveConnectionPoolConfig(
        @Value("${streamer.google.drive.maxTotal:-1}") int maxTotal,
        @Value("${streamer.google.drive.maxTotalPerKey:100}") int maxTotalPerKey,
        @Value("${streamer.google.drive.minIdlePerKey:0}") int minIdlePerKey,
        @Value("${streamer.google.drive.maxIdlePerKey:2}") int maxIdlePerKey) {

        setMaxTotal(maxTotal);
        setMaxTotalPerKey(maxTotalPerKey);
        setMinIdlePerKey(minIdlePerKey);
        setMaxIdlePerKey(maxIdlePerKey);
        setBlockWhenExhausted(false);
    }
}
