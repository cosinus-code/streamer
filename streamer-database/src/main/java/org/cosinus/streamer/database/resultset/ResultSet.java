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
package org.cosinus.streamer.database.resultset;

import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.database.connection.DatabaseException;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static java.util.Optional.of;

public class ResultSet implements AutoCloseable {

    private final java.sql.ResultSet resultSet;

    public ResultSet(java.sql.ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public Value getValue(int columnIndex) {
        try {
            return of(getMetaData().getColumnType(columnIndex))
                .map(ValueResolver::of)
                .map(type -> type.resolveValue(resultSet, columnIndex))
                .orElse(null);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public String getString(String columnLabel) {
        return get(resultSet -> resultSet.getString(columnLabel));
    }

    public Long getLong(int columnIndex) {
        return get(resultSet -> resultSet.getLong(columnIndex));
    }

    private <T> T get(ResultSetValueSupplier<T> supplier) {
        try {
            T value = supplier.supply(resultSet);
            return resultSet.wasNull() ? null : value;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean next() {
        try {
            return resultSet.next();
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    public ResultSetMetaData getMetaData() {
        try {
            return resultSet.getMetaData();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void close() {
        try {
            resultSet.close();
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }
}
