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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.remote.ConnectionPool;
import org.cosinus.streamer.api.remote.RemoteStreamer;
import org.cosinus.streamer.database.connection.DatabaseConnection;
import org.cosinus.streamer.database.connection.DatabaseConnectionPool;
import org.cosinus.streamer.database.connection.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class DatabaseStreamer implements RemoteStreamer<DatabaseRecord, ResultSet, DatabaseConnection> {

    @Autowired
    protected DatabaseConnectionPool connectionPool;

    protected final String connectionName;

    public DatabaseStreamer(String connectionName) {
        injectContext(this);
        this.connectionName = connectionName;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return null;
    }

    @Override
    public String connectionName() {
        return connectionName;
    }

    @Override
    public ConnectionPool<DatabaseConnection, ResultSet> connectionPool() {
        return connectionPool;
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(getName());
    }

    public String getResultSetValue(final ResultSet resultSet, String fieldName) {
        try {
            return resultSet.getString(fieldName);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
