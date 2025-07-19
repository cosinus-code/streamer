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
package org.cosinus.streamer.database.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.Connection;
import org.cosinus.streamer.database.DatabaseStream;
import org.cosinus.streamer.database.resultset.ResultSet;
import org.cosinus.streamer.database.resultset.ResultSetSupplier;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.impl.DefaultDataType;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.jooq.impl.DSL.*;

public class DatabaseConnection implements Connection<ResultSet> {

    public static final String TABLE_SCHEMA = "TABLE_SCHEM";
    public static final String TABLE_CATALOG = "TABLE_CATALOG";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String TABLE_TYPE = "TABLE_TYPE";
    public static final String COLUMN_NAME = "COLUMN_NAME";

    private static final Logger LOG = LogManager.getLogger(DatabaseConnection.class);

    private final java.sql.Connection connection;

    private final DSLContext context;

    private final String currentSchema;

    private final String connectionName;

    public DatabaseConnection(final String connectionName,
                              final java.sql.Connection connection,
                              final String currentSchema) {
        this.connectionName = connectionName;
        this.connection = connection;
        this.context = using(connection);
        this.currentSchema = currentSchema;

    }

    public String getCurrentSchema() {
        return currentSchema;
    }

    @Override
    public String getKey() {
        return connectionName;
    }

    @Override
    public Stream<ResultSet> stream(String query) {
        return DatabaseStream.of(resultSet(query));
    }

    public ResultSet getTables(String schemaName, DatabaseObjectType objectType) {
        return resultSet(() -> connection.getMetaData()
            .getTables(null, schemaName, "%", toArray(objectType.name())));
    }

    public ResultSet getTableFields(String tableName) {
        return resultSet(() -> connection.getMetaData()
            .getColumns(null, currentSchema, tableName, null));
    }

    public ResultSet getTablePrimaryKeys(String tableName) {
        return resultSet(() -> connection.getMetaData()
            .getPrimaryKeys(null, currentSchema, tableName));
    }

    public ResultSet getSchemas() {
        return resultSet(() -> connection.getMetaData().getSchemas());
    }

    public ResultSet resultSet(String query) {
        return resultSet(() -> connection.createStatement().executeQuery(query));
    }

    public Stream<String> getFieldNames(String query) {
        ResultSet resultSet = resultSet(query);
        return resultSet.getFieldNames()
            .onClose(resultSet::close);
    }

    public long getLong(String query) {
        try (ResultSet count = resultSet(query)) {
            return count.next() ? count.getLong(1) : 0;
        }
    }

    public void runQuery(String query) {
        try {
            LOG.info("Execute query: " + query);
            connection.createStatement().execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ResultSet resultSet(final ResultSetSupplier resultSetSupplier) {
        try {
            return new ResultSet(resultSetSupplier.get());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public InputStream inputStream(String query) {
        return null;
    }

    @Override
    public OutputStream outputStream(ResultSet resultSet, String query, boolean append) {
        return null;
    }

    @Override
    public ResultSet save(ResultSet remoteToSave) {
        return null;
    }

    @Override
    public boolean delete(ResultSet remote, boolean moveToTrash) {
        return false;
    }

    boolean isValid() {
        try {
            return connection.isValid(10);
        } catch (SQLException e) {
            LOG.warn("Cannot check the sql connection validity", e);
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOG.warn("Cannot check the sql connection validity", e);
        }
    }

    public java.sql.Connection getConnection() {
        return connection;
    }

    public void insertRecord(String tableName, final Map<String, Object> fieldValues) {
        runQuery(insertRecordQuery(tableName, fieldValues));
    }

    public String insertRecordQuery(String tableName, final Map<String, Object> fieldValues) {
        return context.insertInto(table(name(tableName)))
            .set(fieldValues)
            .toString();
    }

    public void updateRecord(String tableName,
                             final Map<String, Object> primaryKey,
                             final Map<String, Object> fieldValuesToUpdate) {
        runQuery(updateRecordQuery(tableName, primaryKey, fieldValuesToUpdate));
    }

    public String updateRecordQuery(String tableName,
                                    final Map<String, Object> primaryKey,
                                    final Map<String, Object> fieldValuesToUpdate) {
        return context.update(table(name(tableName)))
            .set(fieldValuesToUpdate)
            .where(primaryKey
                .keySet()
                .stream()
                .map(key -> field(name(key)).eq(primaryKey.get(key)))
                .reduce(Condition::and)
                .orElseThrow(() -> new DatabaseException("Cannot update record without primary key")))
            .toString();
    }

    public void createTable(String tableName, Map<String, Integer> fields, String... primaryKey) {
        runQuery(createTableQuery(tableName, fields, primaryKey));
    }

    public String createTableQuery(String tableName, Map<String, Integer> fields, String... primaryKey) {
        return context.createTable(name(currentSchema, tableName))
            .columns(fields.entrySet()
                .stream()
                .map(entry -> field(
                    name(entry.getKey()),
                    type(entry.getValue())))
                .collect(Collectors.toList()))
            .primaryKey(primaryKey)
            .toString();
    }

    private DataType<?> type(int sqlType) {
        return DefaultDataType.getDataType(context.dialect(), sqlType);
    }

    public void dropTable(String tableName) {
        runQuery(context.dropTable(tableName).toString());
    }
}
