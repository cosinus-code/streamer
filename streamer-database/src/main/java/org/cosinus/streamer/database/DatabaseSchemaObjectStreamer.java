package org.cosinus.streamer.database;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DatabaseSchemaObjectStreamer<T extends Streamer<?>> extends DatabaseParentStreamer<T> {

    @Autowired
    private Translator translator;

    private final DatabaseSchemaStreamer schemaStreamer;

    private String name;

    protected final String schemaName;

    public DatabaseSchemaObjectStreamer(String schemaObjectType, String schemaName, String connectionName) {
        super(connectionName);
        this.schemaName = schemaName;
        this.name = translator.translate(schemaObjectType);
        this.schemaStreamer = new DatabaseSchemaStreamer(schemaName, connectionName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DatabaseSchemaStreamer getParent() {
        return schemaStreamer;
    }
}
