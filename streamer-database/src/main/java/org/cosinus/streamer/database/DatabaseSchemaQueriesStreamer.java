package org.cosinus.streamer.database;

import org.apache.commons.io.FilenameUtils;
import org.cosinus.streamer.database.connection.DatabaseObjectType;
import org.cosinus.streamer.database.resultset.ResultSet;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public class DatabaseSchemaQueriesStreamer extends DatabaseSchemaObjectStreamer<DatabaseQueryStreamer> {

    public static final String QUERY_FILE_EXTENSION = ".query";

    public static final ResourceLocator DATABASE_QUERIES = () -> "database/queries";

    @Autowired
    private FilesystemResourceResolver resourceResolver;

    public DatabaseSchemaQueriesStreamer(DatabaseObjectType objectType, String schemaName, String connectionName) {
        super(objectType.getKey(), schemaName, connectionName);
    }

    @Override
    public Stream<DatabaseQueryStreamer> stream() {
        return resourceResolver.resolveResources(DATABASE_QUERIES, QUERY_FILE_EXTENSION)
            .map(this::createDatabaseQueryStreamer)
            .filter(Objects::nonNull);
    }

    public DatabaseQueryStreamer createDatabaseQueryStreamer(String queryFileName) {
        return resourceResolver.resolveAsBytes(DATABASE_QUERIES, queryFileName)
            .map(String::new)
            .map(query -> new DatabaseQueryStreamer(
                FilenameUtils.getBaseName(queryFileName), query, schemaName, connectionName))
            .orElse(null);
    }

    @Override
    public DatabaseQueryStreamer create(Path path, boolean parent) {
        String queryName = path.getFileName().toString();
        return new DatabaseQueryStreamer(queryName, "", schemaName, connectionName);
    }

    @Override
    public DatabaseQueryStreamer createFromRemote(ResultSet remote) {
        return null;
    }

    @Override
    public String getStreamQuery() {
        return null;
    }
}
