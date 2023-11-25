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
package org.cosinus.streamer.database;

import org.cosinus.streamer.api.ParentStreamer;

import java.sql.ResultSet;

import static org.cosinus.streamer.database.connection.DatabaseConnection.*;

public class DatabaseSchemaStreamer extends DatabaseParentStreamer<DatabaseTableStreamer> {

    private final DatabaseConnectionStreamer parent;

    private final String schemaName;

    public DatabaseSchemaStreamer(String schemaName, String connectionName) {
        super(connectionName);
        this.schemaName = schemaName;
        parent = new DatabaseConnectionStreamer(connectionName);
    }

    @Override
    public DatabaseTableStreamer createFromRemote(ResultSet resultSet) {
        String tableName = getResultSetValue(resultSet, TABLE_NAME);
        String tableType = getResultSetValue(resultSet, TABLE_TYPE);
        String tableSchema = getResultSetValue(resultSet, TABLE_SCHEMA);
        return new DatabaseTableStreamer(tableName, tableType, tableSchema, connectionName);
    }

    @Override
    public ParentStreamer<?> getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return schemaName;
    }

    @Override
    public String getStreamQuery() {
        return schemaName;
    }
}
