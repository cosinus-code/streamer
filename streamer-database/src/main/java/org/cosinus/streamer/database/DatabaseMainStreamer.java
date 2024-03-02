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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.database.connection.DatabaseObjectType;
import org.cosinus.streamer.database.model.DatabaseConnectionModel;
import org.cosinus.streamer.database.model.DatabaseModelProvider;
import org.cosinus.swing.translate.Translator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.database.connection.DatabaseObjectType.QUERY;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FILE_SERVER;

@RootStreamer("Database")
@ConditionalOnProperty(name = "streamer.database.enabled", matchIfMissing = true)
public class DatabaseMainStreamer extends MainStreamer<DatabaseConnectionStreamer> {

    public static final String DATABASE_PROTOCOL = "db://";

    private final Map<String, DatabaseConnectionModel> databaseConnectionModelMap;

    private final Translator translator;

    public DatabaseMainStreamer(final Translator translator, final DatabaseModelProvider databaseModelProvider) {
        this.translator = translator;
        this.databaseConnectionModelMap = databaseModelProvider.getConnectionModelsMap();
    }

    @Override
    public Stream<DatabaseConnectionStreamer> stream() {
        return databaseConnectionModelMap.keySet()
            .stream()
            .map(DatabaseConnectionStreamer::new);
    }

    @Override
    public String getProtocol() {
        return DATABASE_PROTOCOL;
    }

    @Override
    public Optional<Streamer<?>> findByPath(Path path) {
        List<String> names = IntStream.range(0, path.getNameCount())
            .mapToObj(path::getName)
            .map(Object::toString)
            .toList();

        String connectionName = !names.isEmpty() ? databaseConnectionModelMap.keySet()
            .stream()
            .filter(names.get(0)::equals)
            .findFirst()
            .orElse(null) : null;

        String schemaName = names.size() > 1 ? names.get(1) : null;
        DatabaseObjectType databaseObjectType = names.size() > 2 ? getDatabaseObjectType(names.get(2)) : null;
        String databaseObjectName = names.size() > 3 ? names.get(3) : null;

        Streamer<?> streamer = connectionName != null ?
            schemaName != null ?
                databaseObjectType != null ?
                    databaseObjectName != null ?
                        databaseObjectType == QUERY ?
                            new DatabaseSchemaQueriesStreamer(QUERY, schemaName, connectionName)
                                .createDatabaseQueryStreamer(databaseObjectName) :
                            new DatabaseTableStreamer(
                                databaseObjectName, databaseObjectType.name(), schemaName, connectionName) :
                        databaseObjectType.getDatabaseSchemaObjectStreamer(schemaName, connectionName) :
                    new DatabaseSchemaStreamer(schemaName, connectionName) :
                new DatabaseConnectionStreamer(connectionName) :
            null;
        return ofNullable(streamer);
    }

    private DatabaseObjectType getDatabaseObjectType(String pathName) {
        return Arrays.stream(DatabaseObjectType.values())
            .filter(objectType -> translator.translate(objectType.getKey()).equals(pathName))
            .findFirst()
            .orElse(null);

    }

    @Override
    public String getIconName() {
        return ICON_FILE_SERVER;
    }
}
