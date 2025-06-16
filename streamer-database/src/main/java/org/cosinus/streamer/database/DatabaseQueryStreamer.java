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
