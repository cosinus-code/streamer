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
import org.cosinus.streamer.database.connection.DatabaseConnection;
import org.cosinus.streamer.database.resultset.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.cosinus.streamer.database.connection.DatabaseConnection.TABLE_SCHEMA;

public class DatabaseConnectionStreamer extends DatabaseParentStreamer<ParentStreamer<?>> {

    @Autowired
    private DatabaseMainStreamer parent;

    public DatabaseConnectionStreamer(String connectionName) {
        super(connectionName);
    }

    @Override
    public Stream<ParentStreamer<?>> stream() {
        String currentSchema = getFromRemote(DatabaseConnection::getCurrentSchema);
        return Stream.of(new DatabaseSchemaStreamer(currentSchema, connectionName));
    }

    @Override
    public ParentStreamer<?> createFromRemote(ResultSet resultSet) {
        return new DatabaseSchemaStreamer(resultSet.getString(TABLE_SCHEMA), connectionName);
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
