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
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.database.connection.DatabaseConnection.TABLE_SCHEMA;

public class DatabaseConnectionStreamer extends DatabaseParentStreamer<DatabaseSchemaStreamer> {

    @Autowired
    private DatabaseMainStreamer parent;

    public DatabaseConnectionStreamer(String connectionName) {
        super(connectionName);
    }

    @Override
    public Stream<DatabaseSchemaStreamer> stream() {
        return getConnection()
            .map(connection -> ofNullable(connection.getCurrentSchema())
                .map(currentSchema -> new DatabaseSchemaStreamer(currentSchema, connectionName))
                .stream()
                .onClose(() -> returnConnection(connection)))
            .orElseGet(Stream::empty);
    }

    @Override
    public DatabaseSchemaStreamer createFromRemote(ResultSet resultSet) {
        return new DatabaseSchemaStreamer(getResultSetValue(resultSet, TABLE_SCHEMA), connectionName);
    }

    @Override
    public String getName() {
        return connectionName;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return parent;
    }

    @Override
    public Path getPath() {
        return Paths.get(connectionName);
    }

    @Override
    public String getStreamQuery() {
        return TABLE_SCHEMA;
    }
}
