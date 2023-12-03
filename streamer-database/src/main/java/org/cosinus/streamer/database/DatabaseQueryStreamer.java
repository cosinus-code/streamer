package org.cosinus.streamer.database;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.value.TranslatableName;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.cosinus.streamer.database.connection.DatabaseObjectType.QUERY;

public class DatabaseQueryStreamer extends DatabaseStreamer {

    private final String queryName;

    private final String query;

    private final DatabaseSchemaQueriesStreamer parent;

    private final DatabaseQueryBinaryStreamer binaryStremer;

    public DatabaseQueryStreamer(String queryName, String query, String schemaName, String connectionName) {
        super(connectionName);
        this.queryName = queryName;
        this.query = query;
        this.parent = new DatabaseSchemaQueriesStreamer(QUERY, schemaName, connectionName);
        this.binaryStremer = new DatabaseQueryBinaryStreamer(this);
    }

    @Override
    public void init() {
        try (Stream<String> fieldNames = getFromRemote(connection -> connection.getFieldNames(getStreamQuery()))) {
            detailNames = fieldNames
                .map(TranslatableName::new)
                .collect(toList());
        }
    }

    @Override
    public boolean canUpdateRecords() {
        return false;
    }

    @Override
    public boolean canUpdateRecordDetail(int detailIndex) {
        return false;
    }

    @Override
    public void updateRecord(DatabaseRecord databaseRecord) {
    }

    @Override
    public void insertRecord(DatabaseRecord databaseRecord) {
    }

    @Override
    public BinaryStreamer binaryStreamer() {
        return binaryStremer;
    }

    @Override
    public String getName() {
        return queryName;
    }

    @Override
    public DatabaseSchemaQueriesStreamer getParent() {
        return parent;
    }

    @Override
    public boolean isTextCompatible() {
        return true;
    }

    @Override
    public void save() {
        binaryStremer.save();
    }

    @Override
    public String getStreamQuery() {
        return query;
    }
}
