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
