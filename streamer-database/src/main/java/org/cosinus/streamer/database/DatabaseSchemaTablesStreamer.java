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

package org.cosinus.streamer.database;

import org.cosinus.streamer.database.connection.DatabaseObjectType;
import org.cosinus.streamer.database.resultset.ResultSet;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.cosinus.streamer.database.connection.DatabaseConnection.*;

public class DatabaseSchemaTablesStreamer extends DatabaseSchemaObjectStreamer<DatabaseTableStreamer> {

    private final DatabaseObjectType objectType;

    public DatabaseSchemaTablesStreamer(DatabaseObjectType objectType, String schemaName, String connectionName) {
        super(objectType.getKey(), schemaName, connectionName);
        this.objectType = objectType;
    }

    @Override
    public Stream<DatabaseTableStreamer> stream() {
        return streamFromRemote(connection -> DatabaseStream.of(connection.getTables(schemaName, objectType)))
            .map(this::createFromRemote);
    }

    @Override
    public DatabaseTableStreamer createFromRemote(ResultSet resultSet) {
        String tableName = resultSet.getString(TABLE_NAME);
        String tableType = resultSet.getString(TABLE_TYPE);
        String tableSchema = resultSet.getString(TABLE_SCHEMA);
        return new DatabaseTableStreamer(tableName, tableType, tableSchema, connectionName);
    }

    @Override
    public DatabaseTableStreamer create(Path path, boolean parent) {
        String tableName = path.getFileName().toString();
        return new DatabaseTableStreamer(tableName, objectType.name(), schemaName, connectionName);
    }

    @Override
    public String getStreamQuery() {
        //TODO
        return null;
    }

    @Override
    public ResultSet getRemote() {
        //TODO
        return null;
    }
}
