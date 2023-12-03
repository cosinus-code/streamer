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
package org.cosinus.streamer.database.connection;

import org.cosinus.streamer.database.DatabaseSchemaObjectStreamer;
import org.cosinus.streamer.database.DatabaseSchemaObjectStreamerSupplier;
import org.cosinus.streamer.database.DatabaseSchemaQueriesStreamer;
import org.cosinus.streamer.database.DatabaseSchemaTablesStreamer;

public enum DatabaseObjectType {
    TABLE("database-type-table", DatabaseSchemaTablesStreamer::new),
    VIEW("database-type-view", DatabaseSchemaTablesStreamer::new),
    SEQUENCE("database-type-sequence", DatabaseSchemaTablesStreamer::new),
    QUERY("database-type-query", DatabaseSchemaQueriesStreamer::new);

    private final String key;

    private final DatabaseSchemaObjectStreamerSupplier streamerSupplier;

    DatabaseObjectType(String key, DatabaseSchemaObjectStreamerSupplier streamerSupplier) {
        this.key = key;
        this.streamerSupplier = streamerSupplier;
    }

    public String getKey() {
        return key;
    }

    public DatabaseSchemaObjectStreamer<?> getDatabaseSchemaObjectStreamer(String schemaName, String connectionName) {
        return streamerSupplier.get(this, schemaName, connectionName);
    }
}
