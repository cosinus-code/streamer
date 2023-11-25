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

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cosinus.streamer.database.DatabaseMainStreamer.DATABASE_PROTOCOL;

public class DatabaseRecord extends LinkedHashMap<TranslatableName, Value> implements Streamable {

    private final DatabaseTableStreamer parent;

    private String name;

    public DatabaseRecord(final DatabaseTableStreamer parent) {
        this.parent = parent;
    }

    public DatabaseTableStreamer getParent() {
        return parent;
    }

    @Override
    public String getProtocol() {
        return DATABASE_PROTOCOL;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return parent.detailNames();
    }

    @Override
    public Map<TranslatableName, Value> details() {
        return this;
    }
}
