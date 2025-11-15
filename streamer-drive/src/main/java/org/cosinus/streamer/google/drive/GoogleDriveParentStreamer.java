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
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.streamer.api.remote.RemoteParentStreamer;
import org.cosinus.streamer.google.drive.connection.GoogleDriveConnection;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Collections;

import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveConnection.*;

public class GoogleDriveParentStreamer
    extends GoogleDriveStreamer<GoogleDriveStreamer<?>>
    implements RemoteParentStreamer<GoogleDriveStreamer<?>, File, GoogleDriveConnection> {

    public final static String LOCAL = "local";

    @Autowired
    private MimeTypeResolver mimeTypeResolver;

    protected long totalSpace;

    protected long freeSpace;

    public GoogleDriveParentStreamer(File file, Path path, String userId) {
        super(file, path, userId);
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
    public GoogleDriveStreamer<?> createFromRemote(File file) {
        return createFromRemoteFileWithPath(file, path.resolve(file.getName()));
    }

    public GoogleDriveStreamer<?> createFromRemoteFileWithPath(File file, Path path) {
        boolean localFile = ofNullable(file.get(LOCAL))
            .map(Object::toString)
            .map(Boolean::parseBoolean)
            .orElse(false);
        if (!localFile) {
            cache.cacheFile(path, file);
        }
        return FOLDER_MIME_TYPE.equals(file.getMimeType()) ?
            new GoogleDriveParentStreamer(file, path, userId) :
            new GoogleDriveBinaryStreamer(file, path, userId);
    }

    @Override
    public void save() {
        File file = getFromRemote(connection -> connection.save(getRemote()));
        if (file == null) {
            throw new SaveStreamerException("Failed to save streamer:" + getPath().toString());
        }

        cache.cacheFile(path, file);
    }

    @Override
    public GoogleDriveStreamer<?> create(Path path, boolean parent) {
        File remoteFile = createLocalFile(path, parent, file.getId());
        GoogleDriveStreamer<?> streamer = createFromRemote(remoteFile);
        streamer.setExists(false);
        return streamer;
    }

    @Override
    public GoogleDriveStreamer<?> create(Path path, Streamer<?> source) {
        String parentId = ofNullable(path.getParent())
            .flatMap(parentPath -> cache.findCachedFile(parentPath)
                .map(File::getId)
                .or(() -> getFromRemote(connection -> connection.findFileByPath(parentPath)
                    .map(File::getId))))
            .orElse(ROOT_PARENT_ID);
        File localFile = createLocalFile(path, source.isParent(), parentId);
        if (!source.isParent()) {
            localFile.set(PROPERTY_TOTAL_TO_UPLOAD, source.getSize());
        }
        localFile.put(PROPERTY_TOTAL_SPACE, totalSpace);
        localFile.put(PROPERTY_FREE_SPACE, freeSpace);
        GoogleDriveStreamer<?> streamer = createFromRemote(localFile);
        streamer.setExists(false);
        return streamer;
    }

    private File createLocalFile(Path path, boolean isParent, String parentId) {
        File localFile = new File()
            .setName(path.getFileName().toString())
            .setParents(ofNullable(parentId)
                .map(Collections::singletonList)
                .orElse(null))
            .setMimeType(isParent ? FOLDER_MIME_TYPE : getMimeTypeForPath(path));

        localFile.put(LOCAL, true);
        return localFile;
    }

    private String getMimeTypeForPath(Path path) {
        return mimeTypeResolver.getMimeTypes(path, false)
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
