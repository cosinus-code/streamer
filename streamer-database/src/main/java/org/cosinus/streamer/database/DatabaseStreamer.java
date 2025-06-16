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

import org.cosinus.streamer.api.remote.ConnectionPool;
import org.cosinus.streamer.api.remote.RemoteStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.database.connection.DatabaseConnection;
import org.cosinus.streamer.database.connection.DatabaseConnectionPool;
import org.cosinus.streamer.database.connection.DatabaseException;
import org.cosinus.streamer.database.resultset.ResultSet;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.cosinus.streamer.database.DatabaseMainStreamer.DATABASE_PROTOCOL;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class DatabaseStreamer implements RemoteStreamer<DatabaseRecord, ResultSet, DatabaseConnection> {

    @Autowired
    protected DatabaseConnectionPool connectionPool;

    protected final String connectionName;

    protected List<TranslatableName> detailNames;

    public DatabaseStreamer(String connectionName) {
        injectContext(this);
        this.connectionName = connectionName;
    }

    @Override
    public Stream<DatabaseRecord> stream() {
        final AtomicInteger index = new AtomicInteger();
        return streamFromRemote(connection -> connection.stream(getStreamQuery()))
            .map(resultSet -> createRecord(resultSet, index.getAndIncrement()));
    }

    private DatabaseRecord createRecord(ResultSet resultSet, int recordIndex) {
        DatabaseRecord databaseRecord = new DatabaseRecord(this, Integer.toString(recordIndex));
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            IntStream.rangeClosed(1, metaData.getColumnCount())
                .forEach(index -> {
                    try {
                        String columnName = metaData.getColumnName(index);
                        Value value = resultSet.getValue(index);
                        databaseRecord.put(new TranslatableName(columnName), value);
                        //TODO:
                        if (columnName.equalsIgnoreCase("id") && value != null) {
                            databaseRecord.setName(value.toString());
                            databaseRecord.setLeadDetailIndex(index - 1);
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
    public String getProtocol() {
        return DATABASE_PROTOCOL;
    }

    @Override
    public String connectionId() {
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

    @Override
    public ResultSet getRemote() {
        //TODO
        return null;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public boolean isParent() {
        return true;
    }

    public abstract boolean canUpdateRecords();

    public abstract boolean canUpdateRecordDetail(int detailIndex);

    public abstract void updateRecord(DatabaseRecord databaseRecord);

    public abstract void insertRecord(DatabaseRecord databaseRecord);
}
