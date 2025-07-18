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

package org.cosinus.streamer.google.drive.connection;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About.StorageQuota;
import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.Connection;
import org.cosinus.swing.context.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.api.client.googleapis.media.MediaHttpUploader.CONTENT_LENGTH_HEADER;
import static com.google.api.client.googleapis.media.MediaHttpUploader.CONTENT_TYPE_HEADER;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveConnectionFactory.GSON_FACTORY;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class GoogleDriveConnection implements Connection<File> {

    private static final Logger LOG = LogManager.getLogger(GoogleDriveConnection.class);

    public static final String DRIVE = "drive";

    public static final String PROPERTY_UPLOAD_TYPE = "uploadType";

    public static final String PROPERTY_TOTAL_TO_UPLOAD = "uploadTotal";

    public static final String PROPERTY_TOTAL_SPACE = "totalSpace";

    public static final String PROPERTY_FREE_SPACE = "freeSpace";

    public static final String HEADER_CONTENT_RANGE = "bytes %d-%d/%d";

    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";

    public static final String FILE_MIME_TYPE = "application/octet-stream";

    public static final String STORAGE_QUOTA_FIELDS = "storageQuota(limit,usage)";

    public static final String FILE_FIELDS = "id,name,mimeType,parents,size,modifiedTime";

    public static final String FILES_FIELDS = "files(%s)".formatted(FILE_FIELDS);

    public static final String QUERY_FOR_PARENT = "parents in '%s'";

    public static final String QUERY_FOR_NAME = "name = '%s'";

    public static final String QUERY_NO_TRASH = "trashed = false";

    public static final String AND = " and ";

    public static final String ROOT_PARENT_ID = "root";

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private GoogleDriveCache cache;

    private final AuthorizationCodeInstalledApp googleAuthenticator;

    private final NetHttpTransport httpTransport;

    private final String userId;

    private Drive client;

    private Credential googleCredentials;

    private StorageQuota storageQuota;

    public GoogleDriveConnection(final AuthorizationCodeInstalledApp googleAuthenticator,
                                 final NetHttpTransport httpTransport,
                                 final String userId) throws IOException {
        injectContext(this);
        this.googleAuthenticator = googleAuthenticator;
        this.httpTransport = httpTransport;
        this.userId = userId;
        initializeGoogleCredential();
    }

    protected void initializeGoogleCredential() throws IOException {
        this.googleCredentials = googleAuthenticator.authorize(userId);
        if (this.googleCredentials.getAccessToken() == null) {
            googleAuthenticator.getFlow().getCredentialDataStore().delete(userId);
            this.googleCredentials = googleAuthenticator.authorize(userId);
        }
        LOG.info("Google Drive credentials initialized for user {}", userId);
        this.client = new Drive.Builder(httpTransport, GSON_FACTORY, googleCredentials)
            .setApplicationName(applicationProperties.getName())
            .build();
        this.storageQuota = storageQuota();
    }

    public long getTotalSpace() {
        return ofNullable(storageQuota)
            .map(StorageQuota::getLimit)
            .orElse(-1L);
    }

    public long getFreeSpace() {
        return ofNullable(storageQuota)
            .map(quota -> quota.getLimit() - quota.getUsage())
            .orElse(-1L);
    }

    protected boolean areGoogleCredentialsValid() {
        return googleCredentials != null
            && (googleCredentials.getRefreshToken() != null
            || googleCredentials.getExpiresInSeconds() == null
            || googleCredentials.getExpiresInSeconds() > 0);
    }

    @Override
    public String getKey() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    public StorageQuota storageQuota() {
        try {
            return client
                .about()
                .get()
                .setFields(STORAGE_QUOTA_FIELDS)
                .execute().getStorageQuota();
        } catch (IOException ex) {
            LOG.error("Failed to get Google Drive storage quota.", ex);
            return null;
        }
    }

    @Override
    public Stream<File> stream(String query) {
        try {
            List<File> files = client
                .files()
                .list()
                .setSpaces(DRIVE)
                .setQ(query)
                .setFields(FILES_FIELDS)
                .execute()
                .getFiles();
            files.forEach(this::populateFile);

            return files.stream();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to stream Google Drive files", e);
        }
    }

    public Optional<File> findFileByPath(Path path) {
        return findFilesByName(path.getFileName().toString())
            .stream()
            .filter(file -> isFileCorrespondingToPath(file, path))
            .findFirst()
            .map(this::populateFile);
    }

    private boolean isFileCorrespondingToPath(final File file, final Path path) {
        if (isRootPath(path) && isRootFile(file)) {
            return true;
        }
        if (isRootPath(path) || !file.getName().equals(path.getFileName().toString())) {
            return false;
        }

        String parentId = file
            .getParents()
            .stream()
            .findFirst()
            .orElse(null);
        File parentFile = findFilesById(parentId);
        Path parentPath = path.getParent();

        boolean isFileCorrespondingToPath = isFileCorrespondingToPath(parentFile, parentPath);
        if (isFileCorrespondingToPath) {
            cache.cacheFile(path, file);
        }
        return isFileCorrespondingToPath;
    }

    private boolean isRootPath(Path path) {
        return path == null || path.getFileName().toString().equals(userId);
    }

    private boolean isRootFile(File file) {
        return isEmpty(file.getParents());
    }

    public File findFilesById(String fileId) {
        try {
            return populateFile(client
                .files()
                .get(fileId)
                .setFields(FILE_FIELDS)
                .execute());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to search Google Drive files by id", e);
        }
    }

    public List<File> findFilesByName(String fileName) {
        try {
            return client
                .files()
                .list()
                .setSpaces(DRIVE)
                .setQ(QUERY_FOR_NAME.formatted(fileName))
                .setFields(FILES_FIELDS)
                .execute()
                .getFiles();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to search Google Drive files by name", e);
        }
    }

    private File populateFile(File file) {
        file.put(PROPERTY_TOTAL_SPACE, getTotalSpace());
        file.put(PROPERTY_FREE_SPACE, getFreeSpace());
        return file;
    }

    @Override
    public InputStream inputStream(String fileId) {
        try {
            return client
                .files()
                .get(fileId)
                .executeMediaAsInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create Google Drive input stream for file: " + fileId, e);
        }
    }

    @Override
    public OutputStream outputStream(String query, boolean append) {
        return null;
    }

    @Override
    public File save(File fileToSave) {
        try {
            return client
                .files()
                .create(fileToSave)
                .setFields(FILE_FIELDS)
                .execute();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create Google Drive file: " + fileToSave.getName(), e);
        }
    }

    @Override
    public boolean delete(File fileToDelete, boolean moveToTrash) {
        try {
            if (moveToTrash) {
                client
                    .files()
                    .update(fileToDelete.getId(), new File().setTrashed(true))
                    .execute();
            } else {
                client
                    .files()
                    .delete(fileToDelete.getId())
                    .execute();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete Google Drive file: " + fileToDelete.getName(), e);
        }
        return true;
    }

    public File createFileAndStartResumableUpload(File fileToCreate) {
        try {
            long totalToUpload = ofNullable(fileToCreate.get(PROPERTY_TOTAL_TO_UPLOAD))
                .map(Object::toString)
                .map(Long::parseLong)
                .orElse(-1L);

            HttpHeaders headers = new HttpHeaders();
            headers.set(CONTENT_TYPE_HEADER, fileToCreate.getMimeType());
            headers.set(CONTENT_LENGTH_HEADER, totalToUpload);

            return new CreateResumableFileUpload(client, fileToCreate)
                .setRequestHeaders(headers)
                .execute();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create Google Drive file for upload: " + fileToCreate.getName(), e);
        }
    }

    public void resumeUpload(File fileToUpdate, byte[] bytes) {
        try (FileUploadRequest request = new FileUploadRequest(client, fileToUpdate, bytes)) {
            long totalBytesCountToUpload = ofNullable(fileToUpdate.get(PROPERTY_TOTAL_TO_UPLOAD))
                .map(Object::toString)
                .map(Long::parseLong)
                .orElseThrow(() -> new IOException("Unknown upload size for file: " + fileToUpdate.getName()));

            long currentUploadedBytesCount = ofNullable(fileToUpdate.getSize())
                .orElse(0L);

            long bytesCountToUpload = currentUploadedBytesCount + bytes.length > totalBytesCountToUpload ?
                totalBytesCountToUpload - currentUploadedBytesCount :
                bytes.length;

            String contentRange = HEADER_CONTENT_RANGE.formatted(
                currentUploadedBytesCount,
                currentUploadedBytesCount + bytesCountToUpload - 1,
                totalBytesCountToUpload);

            request
                .setContentLength(bytesCountToUpload)
                .setContentRange(contentRange)
                .setThrowExceptionOnExecuteError(false)
                .execute();

            if (request.isSuccessStatusCode()) {
                return;
            }

            if (request.getResponseStatusCode() != 308) {
                throw new IOException("Failed to upload content for file: %s. Status code: %d"
                    .formatted(fileToUpdate.getName(), request.getResponseStatusCode()));
            }

            long bytesCountReceivedByServer = ofNullable(request.getResponseRange())
                .map(rangeHeader -> rangeHeader.substring(rangeHeader.indexOf('-') + 1))
                .map(Long::parseLong)
                .map(range -> range + 1)
                .orElse(-1L);

            if (bytesCountReceivedByServer >= 0 && bytesCountToUpload > bytesCountReceivedByServer) {
                throw new IOException("The server received less bytes than expected: %d received but %d was sent"
                    .formatted(bytesCountReceivedByServer, bytesCountToUpload));
            }
            fileToUpdate.setSize(currentUploadedBytesCount + bytesCountToUpload);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload content to Google Drive file: " + fileToUpdate.getName(), e);
        }
    }

    @Override
    public void close() {
    }
}
