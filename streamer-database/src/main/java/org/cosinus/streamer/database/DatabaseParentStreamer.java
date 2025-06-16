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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.remote.ConnectionPool;
import org.cosinus.streamer.api.remote.RemoteParentStreamer;
import org.cosinus.streamer.database.connection.DatabaseConnection;
import org.cosinus.streamer.database.connection.DatabaseConnectionPool;
import org.cosinus.streamer.database.resultset.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

import static org.cosinus.streamer.database.DatabaseMainStreamer.DATABASE_PROTOCOL;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class DatabaseParentStreamer<D extends Streamer<?>>
    implements RemoteParentStreamer<D, ResultSet, DatabaseConnection> {

    @Autowired
    protected DatabaseConnectionPool connectionPool;

    protected final String connectionName;

    public DatabaseParentStreamer(String connectionName) {
        injectContext(this);
        this.connectionName = connectionName;
    }

    @Override
    public String getProtocol() {
        return DATABASE_PROTOCOL;
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(getName());
    }

    @Override
    public String connectionId() {
        return connectionName;
    }

    @Override
    public ConnectionPool<DatabaseConnection, ResultSet> connectionPool() {
        return connectionPool;
    }
}
