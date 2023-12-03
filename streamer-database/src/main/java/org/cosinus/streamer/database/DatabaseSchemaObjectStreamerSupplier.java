package org.cosinus.streamer.database;

import org.cosinus.streamer.database.connection.DatabaseObjectType;

@FunctionalInterface
public interface DatabaseSchemaObjectStreamerSupplier {

    DatabaseSchemaObjectStreamer<?> get(DatabaseObjectType objectType, String schemaName, String connectionName);
}
