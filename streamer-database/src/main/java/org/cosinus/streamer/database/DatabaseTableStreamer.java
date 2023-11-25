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
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.database.connection.DatabaseException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.database.connection.DatabaseConnection.*;


public class DatabaseTableStreamer extends DatabaseStreamer {

    private final String tableName;

    private final String tableType;

    private final DatabaseSchemaStreamer schemaStreamer;

    private List<TranslatableName> detailNames;

    public DatabaseTableStreamer(String tableName, String tableType, String tableSchema, String connectionName) {
        super(connectionName);
        this.tableName = tableName;
        this.tableType = tableType;
        this.schemaStreamer = new DatabaseSchemaStreamer(tableSchema, connectionName);
    }

    @Override
    public Stream<DatabaseRecord> stream() {
        return streamFromRemote(connection -> connection.stream(getStreamQuery()))
            .map(this::createRecord);
    }

    private DatabaseRecord createRecord(ResultSet resultSet) {
        DatabaseRecord databaseRecord = new DatabaseRecord(this);
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            IntStream.rangeClosed(1, metaData.getColumnCount())
                .forEach(index -> {
                    try {
                        String columnName = metaData.getColumnName(index);
                        //int columnType = metaData.getColumnType(index);
                        Value value = ofNullable(resultSet.getObject(index))
                            .map(Object::toString)
                            .map(TextValue::new)
                            .orElse(null);

                        databaseRecord.put(new TranslatableName(columnName), value);
                        if (databaseRecord.getName() == null && value != null) {
                            databaseRecord.setName(value.toString());
                        }
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

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public void initDetails() {
        try (Stream<ResultSet> fieldsStream = resultSetStream(connection -> connection.getTableFields(tableName))) {
            detailNames = fieldsStream
                .map(field -> getResultSetValue(field, COLUMN_NAME))
                .map(TranslatableName::new)
                .collect(Collectors.toList());
        }
    }
}
