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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.swing.image.icon.IconProvider.ICON_DATABASE;

@RootStreamer("Database")
@ConditionalOnProperty(name = "streamer.database.enabled", matchIfMissing = true)
public class DatabaseMainStreamer extends MainStreamer<DatabaseConnectionStreamer> {

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
    public String getIconName() {
        return ICON_DATABASE;
    }

    @Override
    public boolean isCompatible(String urlPath) {
        return false;
    }

    @Override
    public Optional<Streamer> findByUrlPath(String urlPath) {
        return Optional.empty();
    }

    @Override
    public void execute(Path path) {
    }

    @Override
    public boolean exists() {
        return true;
    }
}
