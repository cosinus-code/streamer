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
import java.util.stream.Stream;

import static org.cosinus.streamer.database.connection.DatabaseConnection.*;


public class DatabaseTableStreamer extends DatabaseStreamer {

    private final String tableName;

    private final String tableType;

    private final DatabaseSchemaStreamer schemaStreamer;

    public DatabaseTableStreamer(ResultSet resultSet, String connectionName) {
        super(connectionName);
        this.tableName = getResultSetValue(resultSet, TABLE_NAME);
        this.tableType = getResultSetValue(resultSet, TABLE_TYPE);
        String tableSchema = getResultSetValue(resultSet, TABLE_SCHEMA);
        this.schemaStreamer = new DatabaseSchemaStreamer(tableSchema, connectionName);
    }

    @Override
    public Stream<ResultSet> stream() {
        return Stream.empty();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return schemaStreamer;
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public String getStreamQuery() {
        return "select * from " + tableName;
    }
}
