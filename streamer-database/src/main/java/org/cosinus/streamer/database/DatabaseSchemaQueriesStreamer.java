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

    @Override
    public ResultSet getRemote() {
        //TODO
        return null;
    }
}
