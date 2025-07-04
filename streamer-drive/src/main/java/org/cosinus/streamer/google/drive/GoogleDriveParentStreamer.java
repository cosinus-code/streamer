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

import com.google.api.services.drive.model.File;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.remote.RemoteParentStreamer;
import org.cosinus.streamer.google.drive.connection.GoogleDriveConnection;
import org.cosinus.swing.mimetype.MimeTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

import static java.lang.String.join;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveConnection.*;

public class GoogleDriveParentStreamer
    extends GoogleDriveStreamer<GoogleDriveStreamer<?>>
    implements RemoteParentStreamer<GoogleDriveStreamer<?>, File, GoogleDriveConnection> {

    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    public static final String FILE_MIME_TYPE = "application/octet-stream";

    public static final String QUERY_FOR_PARENT = "parents in '%s'";

    public static final String QUERY_NO_TRASH = "trashed = false";

    public static final String AND = " and ";

    public static final String ROOT_PARENT_ID = "root";

    @Autowired
    private MimeTypeResolver mimeTypeResolver;

    protected long totalSpace;

    protected long freeSpace;

    public GoogleDriveParentStreamer(File file, Path path, ParentStreamer<?> parent, String userId) {
        super(file, path, parent, userId);
        this.totalSpace = ofNullable(file.get(PROPERTY_TOTAL_SPACE))
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .orElse(-1L);
        this.freeSpace = ofNullable(file.get(PROPERTY_FREE_SPACE))
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .orElse(-1L);

    }

    @Override
    public GoogleDriveStreamer<?> createFromRemote(File remoteFile) {
        return FOLDER_MIME_TYPE.equals(remoteFile.getMimeType()) ?
            new GoogleDriveParentStreamer(remoteFile, path.resolve(remoteFile.getName()), this, userId) :
            new GoogleDriveBinaryStreamer(remoteFile, path.resolve(remoteFile.getName()), this, userId);
    }

    @Override
    public GoogleDriveStreamer<?> create(Path path, Streamer<?> source) {
        if (!path.startsWith(this.path)) {
            //TODO: temporary
            throw new IllegalArgumentException("Path '" + path + "' is not a child of '" + this.path + "'");
        }
        File remoteFile = new File()
            .setName(path.getFileName().toString())
            .setParents(singletonList(this.file.getId()))
            .setMimeType(source.isParent() ? FOLDER_MIME_TYPE : getMimeTypeForPath(path));
        if (!source.isParent()) {
            remoteFile.set(PROPERTY_TOTAL_TO_UPLOAD, source.getSize());
        }
        remoteFile.put(PROPERTY_TOTAL_SPACE, totalSpace);
        remoteFile.put(PROPERTY_FREE_SPACE, freeSpace);
        GoogleDriveStreamer<?> streamer = createFromRemote(remoteFile);
        streamer.setExists(false);
        return streamer;
    }

    private String getMimeTypeForPath(Path path) {
        return mimeTypeResolver.getMimeTypes(path)
            .stream()
            .findFirst()
            .map(Object::toString)
            .orElse(FILE_MIME_TYPE);
    }

    @Override
    public String getStreamQuery() {
        return join(AND,
            QUERY_FOR_PARENT.formatted(
                ofNullable(file.getId())
                    .orElse(ROOT_PARENT_ID)),
            QUERY_NO_TRASH);
    }

    @Override
    public long getTotalSpace() {
        return totalSpace;
    }

    @Override
    public long getFreeSpace() {
        return freeSpace;
    }
}
