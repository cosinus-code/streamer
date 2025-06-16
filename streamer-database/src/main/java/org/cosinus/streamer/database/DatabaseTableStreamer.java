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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.database.resultset.ResultSet;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.sql.Types.INTEGER;
import static java.util.stream.Collectors.*;
import static org.cosinus.streamer.database.connection.DatabaseConnection.COLUMN_NAME;


public class DatabaseTableStreamer extends DatabaseStreamer {

    private final String tableName;

    private final String tableType;

    private final DatabaseSchemaStreamer schemaStreamer;

    private Set<String> fields;

    private Set<String> primaryKeys;

    private Long size;

    public DatabaseTableStreamer(String tableName, String tableType, String tableSchema, String connectionName) {
        super(connectionName);
        this.tableName = tableName;
        this.tableType = tableType;
        this.schemaStreamer = new DatabaseSchemaStreamer(tableSchema, connectionName);
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

    @Override
    public long getSize() {
        if (size == null) {
            size = getFromRemote(connection -> connection.getLong("select count(*) from " + tableName));
        }
        return size;
    }

    public Set<String> getPrimaryKeys() {
        return primaryKeys;
    }

    @Override
    public void init() {
        runRemote(connection -> {
            try (Stream<ResultSet> fieldsStream = DatabaseStream.of(connection.getTableFields(tableName))) {
                fields = fieldsStream
                    .map(field -> field.getString(COLUMN_NAME))
                    .collect(toSet());
            }
            try (Stream<ResultSet> pkStream = DatabaseStream.of(connection.getTablePrimaryKeys(tableName))) {
                primaryKeys = pkStream
                    .map(field -> field.getString(COLUMN_NAME))
                    .collect(toSet());
            }
        });
        detailNames = fields
            .stream()
            .map(TranslatableName::new)
            .collect(toList());
    }

    @Override
    public void save() {
        runRemote(connection -> connection.createTable(tableName, Map.of(
                "ID", INTEGER,
                "CREATION", Types.DATE),
            "ID"));
    }

    @Override
    public boolean delete() {
        runRemote(connection -> connection.dropTable(tableName));
        return true;
    }

    @Override
    public boolean canUpdateRecords() {
        return true;
    }

    @Override
    public boolean canUpdateRecordDetail(int detailIndex) {
        return !getPrimaryKeys().contains(detailNames.get(detailIndex).name());
    }

    public void updateRecord(DatabaseRecord databaseRecord) {
        Map<String, Object> fieldValuesToUpdate = new LinkedHashMap<>();
        Map<String, Object> primaryKey = new LinkedHashMap<>();
        databaseRecord.forEach((key, value) -> {
            if (primaryKeys.contains(key.name())) {
                primaryKey.put(key.name(), value.value());
            } else {
                fieldValuesToUpdate.put(key.name(), value.value());
            }
        });
        runRemote(connection -> connection.updateRecord(tableName, primaryKey, fieldValuesToUpdate));
    }

    public void insertRecord(DatabaseRecord databaseRecord) {
        runRemote(connection -> connection.insertRecord(tableName, databaseRecord
            .entrySet()
            .stream()
            .collect(toMap(
                entry -> entry.getKey().name(),
                entry -> entry.getValue().value()))));
    }
}
