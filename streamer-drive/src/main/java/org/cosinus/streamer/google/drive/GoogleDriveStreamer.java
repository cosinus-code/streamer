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

package org.cosinus.streamer.google.drive;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.file.BaseFileStreamer;
import org.cosinus.streamer.api.remote.ConnectionPool;
import org.cosinus.streamer.api.remote.RemoteStreamer;
import org.cosinus.streamer.google.drive.connection.GoogleDriveCache;
import org.cosinus.streamer.google.drive.connection.GoogleDriveConnection;
import org.cosinus.streamer.google.drive.connection.GoogleDriveConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.google.drive.GoogleDriveMainStreamer.DRIVE_PROTOCOL;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class GoogleDriveStreamer<T> extends BaseFileStreamer<T> implements RemoteStreamer<T, File, GoogleDriveConnection> {

    @Autowired
    protected GoogleDriveConnectionPool googleDriveConnectionPool;

    @Autowired
    protected GoogleDriveCache cache;

    protected final File file;

    protected final Path path;

    @Getter
    protected final String userId;

    @Setter
    protected boolean exists = true;

    public GoogleDriveStreamer(File file, Path path, String userId) {
        injectContext(this);
        this.file = file;
        this.path = path;
        this.userId = userId;
    }

    @Override
    public String connectionId() {
        return userId;
    }

    @Override
    public ConnectionPool<GoogleDriveConnection, File> connectionPool() {
        return googleDriveConnectionPool;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public File getRemote() {
        return file;
    }

    @Override
    public long getSize() {
        return ofNullable(file.getSize())
            .map(Long::longValue)
            .orElse(-1L);
    }

    @Override
    public long lastModified() {
        return ofNullable(file.getModifiedTime())
            .map(DateTime::getValue)
            .orElse(0L);
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public String getProtocol() {
        return DRIVE_PROTOCOL;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return ofNullable(path.getParent())
            .flatMap(parentPath -> cache.findCachedFile(parentPath)
                .or(() -> getFromRemote(connection -> connection.findFileByPath(parentPath)))
                .map(parentFile -> new GoogleDriveParentStreamer(parentFile, parentPath, userId)))
            .orElseGet(() -> new GoogleDriveUserStreamer(userId));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GoogleDriveStreamer<?> that)) {
            return false;
        }

        return Objects.equals(file.getId(), that.file.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.getId());
    }
}
