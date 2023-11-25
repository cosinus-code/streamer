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
import org.cosinus.streamer.database.model.DatabaseConnectionModel;
import org.cosinus.streamer.database.model.DatabaseModelProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.cosinus.streamer.database.connection.DatabaseObjectType.TABLE;
import static org.cosinus.swing.image.icon.IconProvider.ICON_DATABASE;

@RootStreamer("Database")
@ConditionalOnProperty(name = "streamer.database.enabled", matchIfMissing = true)
public class DatabaseMainStreamer extends MainStreamer<DatabaseConnectionStreamer> {

    public static final String DATABASE_PROTOCOL = "db://";

    private final Map<String, DatabaseConnectionModel> databaseConnectionModelMap;

    public DatabaseMainStreamer(final DatabaseModelProvider databaseModelProvider) {
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
        String tableName = names.size() > 2 ? names.get(2) : null;

        return connectionName != null ?
            schemaName != null ?
                tableName != null ?
                    Optional.of(new DatabaseTableStreamer(tableName, TABLE.name(), schemaName, connectionName)) :
                    Optional.of(new DatabaseSchemaStreamer(schemaName, connectionName)) :
                Optional.of(new DatabaseConnectionStreamer(connectionName)) :
            super.findByPath(path);
    }

    @Override
    public String getIconName() {
        return ICON_DATABASE;
    }
}
