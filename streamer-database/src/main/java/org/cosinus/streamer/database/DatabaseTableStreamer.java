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
import org.cosinus.streamer.database.connection.DatabaseException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.stream.IntStream;
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
    public Stream<DatabaseRecord> stream() {
        return getConnection()
            .map(connection -> connection
                .stream(getStreamQuery())
                .map(this::createRecord)
                .onClose(() -> returnConnection(connection)))
            .orElseGet(Stream::empty);
    }

    private DatabaseRecord createRecord(ResultSet resultSet) {
        DatabaseRecord databaseRecord = new DatabaseRecord();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            IntStream.rangeClosed(1, metaData.getColumnCount())
                .forEach(index -> {
                    try {
                        String columnName = metaData.getColumnClassName(index);
                        //int columnType = metaData.getColumnType(index);
                        Object columnValue = resultSet.getObject(index);
                        databaseRecord.put(columnName, columnValue);
                    } catch (SQLException e) {
                        throw new DatabaseException(e);
                    }
                });
            return databaseRecord;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
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
