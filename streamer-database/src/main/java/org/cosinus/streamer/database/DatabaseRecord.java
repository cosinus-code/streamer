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
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.cosinus.streamer.database.DatabaseMainStreamer.DATABASE_PROTOCOL;

public class DatabaseRecord extends LinkedHashMap<TranslatableName, Value> implements Streamable {

    private final DatabaseStreamer parent;

    private final String id;

    private final List<Value> details;

    private String primaryKeyFieldName;

    private int leadDetailIndex;

    public DatabaseRecord(final DatabaseStreamer parent, final String id) {
        this.parent = parent;
        this.id = id;
        this.details = new ArrayList<>();
    }

    @Override
    public Value put(TranslatableName key, Value value) {
        details.add(value);
        return super.put(key, value);
    }

    @Override
    public DatabaseStreamer getParent() {
        return parent;
    }

    @Override
    public String getProtocol() {
        return DATABASE_PROTOCOL;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return primaryKeyFieldName;
    }

    public void setName(String primaryKeyFieldName) {
        this.primaryKeyFieldName = primaryKeyFieldName;
    }

    public void setLeadDetailIndex(int leadDetailIndex) {
        this.leadDetailIndex = leadDetailIndex;
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
    public List<Value> details() {
        return details;
    }

    @Override
    public int getLeadDetailIndex() {
        return leadDetailIndex;
    }

    @Override
    public boolean canUpdateDetail(int detailIndex) {
        return parent.canUpdateRecordDetail(detailIndex);
    }

    @Override
    public boolean canUpdate() {
        return parent.canUpdateRecords();
    }

    @Override
    public void save() {
        if (exists()) {
            parent.updateRecord(this);
        } else {
            parent.insertRecord(this);
        }
    }
}
