package org.cosinus.streamer.database;

import org.cosinus.streamer.database.connection.DatabaseObjectType;
import org.cosinus.streamer.database.resultset.ResultSet;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.cosinus.streamer.database.connection.DatabaseConnection.*;

public class DatabaseSchemaTablesStreamer extends DatabaseSchemaObjectStreamer<DatabaseTableStreamer> {

    private final DatabaseObjectType objectType;

    public DatabaseSchemaTablesStreamer(DatabaseObjectType objectType, String schemaName, String connectionName) {
        super(objectType.getKey(), schemaName, connectionName);
        this.objectType = objectType;
    }

    @Override
    public Stream<DatabaseTableStreamer> stream() {
        return streamFromRemote(connection -> DatabaseStream.of(connection.getTables(schemaName, objectType)))
            .map(this::createFromRemote);
    }

    @Override
    public DatabaseTableStreamer createFromRemote(ResultSet resultSet) {
        String tableName = resultSet.getString(TABLE_NAME);
        String tableType = resultSet.getString(TABLE_TYPE);
        String tableSchema = resultSet.getString(TABLE_SCHEMA);
        return new DatabaseTableStreamer(tableName, tableType, tableSchema, connectionName);
    }

    @Override
    public DatabaseTableStreamer create(Path path, boolean parent) {
        String tableName = path.getFileName().toString();
        return new DatabaseTableStreamer(tableName, objectType.name(), schemaName, connectionName);
    }

    @Override
    public String getStreamQuery() {
        return null;
    }
}
