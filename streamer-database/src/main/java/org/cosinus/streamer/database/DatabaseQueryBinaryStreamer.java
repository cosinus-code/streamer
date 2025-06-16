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
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.database.connection.DatabaseException;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Path;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.database.DatabaseMainStreamer.DATABASE_PROTOCOL;
import static org.cosinus.streamer.database.DatabaseSchemaQueriesStreamer.DATABASE_QUERIES;
import static org.cosinus.streamer.database.DatabaseSchemaQueriesStreamer.QUERY_FILE_EXTENSION;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class DatabaseQueryBinaryStreamer implements BinaryStreamer {

    @Autowired
    private FilesystemResourceResolver resourceResolver;

    private final String queryName;

    private final DatabaseQueryStreamer databaseQueryStreamer;

    public DatabaseQueryBinaryStreamer(final DatabaseQueryStreamer databaseQueryStreamer) {
        injectContext(this);
        this.queryName = databaseQueryStreamer.getName();
        this.databaseQueryStreamer = databaseQueryStreamer;
    }

    @Override
    public InputStream inputStream() {
        try {
            return new FileInputStream(getFile());
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputStream outputStream(boolean append) {
        try {
            return new FileOutputStream(getFile(), append);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ParentStreamer<?> getParent() {
        return databaseQueryStreamer.getParent();
    }

    @Override
    public String getName() {
        return databaseQueryStreamer.getName();
    }

    @Override
    public Path getPath() {
        return databaseQueryStreamer.getPath();
    }

    public File getFile() {
        return resourceResolver.resolveResourcePath(DATABASE_QUERIES, queryName + QUERY_FILE_EXTENSION)
            .map(Path::toFile)
            .orElseThrow(() ->
                new DatabaseException("Failed to resolve database query file: " + queryName + QUERY_FILE_EXTENSION));
    }

    @Override
    public String getProtocol() {
        return DATABASE_PROTOCOL;
    }

    public void save() {
        ofNullable(getFile())
            .filter(file -> file.getParentFile().exists() || file.getParentFile().mkdirs())
            .filter(this::createQueryFile)
            .orElseThrow(() ->
                new DatabaseException("Failed to create database query file: " + queryName + QUERY_FILE_EXTENSION));
    }

    private boolean createQueryFile(File file) {
        try {
            return !file.exists() && file.createNewFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
