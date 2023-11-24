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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.Connection;
import org.cosinus.streamer.database.DatabaseStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.cosinus.streamer.database.connection.DatabaseObjectType.TABLE;

public class DatabaseConnection implements Connection<ResultSet> {

    public static final String TABLE_SCHEMA = "TABLE_SCHEM";
    public static final String TABLE_CATALOG = "TABLE_CATALOG";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String TABLE_TYPE = "TABLE_TYPE";
    public static final String COLUMN_NAME = "COLUMN_NAME";

    private static final Logger LOG = LogManager.getLogger(DatabaseConnection.class);

    private final java.sql.Connection connection;

    private final String currentSchema;

    public DatabaseConnection(java.sql.Connection connection, String currentSchema) {
        this.connection = connection;
        this.currentSchema = currentSchema;
    }

    public String getCurrentSchema() {
        return currentSchema;
    }

    @Override
    public Stream<ResultSet> stream(String query) {
        ResultSet resultSet = ofNullable(query)
            .filter(currentSchema::equals)
            .map(this::getTables)
            .orElseGet(() -> resultSet(query));

        return DatabaseStream.of(resultSet);
    }

    public ResultSet getTables(String schemaName) {
        return resultSet(() -> connection.getMetaData()
            .getTables(null, schemaName, "%", toArray(TABLE.name())));
    }

    public ResultSet getTableFields(String tableName) {
        return resultSet(() -> connection.getMetaData()
            .getColumns(null, currentSchema, tableName, null));
    }

    public ResultSet getSchemas() {
        return resultSet(() -> connection.getMetaData().getSchemas());
    }

    public ResultSet resultSet(String query) {
        return resultSet(() -> connection.createStatement().executeQuery(query));
    }

    private ResultSet resultSet(final ResultSetSupplier resultSetSupplier) {
        try {
            return resultSetSupplier.get();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public InputStream inputStream(String query) {
        return null;
    }

    @Override
    public OutputStream outputStream(String query, boolean append) {
        return null;
    }

    @Override
    public boolean save(String query) {
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
}
