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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.model.About.StorageQuota;
import com.google.api.services.drive.model.File;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.Connection;
import org.cosinus.streamer.google.drive.connection.GoogleDriveClient.Builder;
import org.cosinus.swing.context.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    public static final String QUERY_FOR_PARENT = "'%s' in parents";

    public static final String QUERY_BY_NAME = "name = '%s'";

    public static final String QUERY_NO_TRASH = "trashed = false";

    public static final String AND = " and ";

    public static final String ROOT_PARENT_ID = "root";

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private GoogleDriveCache cache;

    private final AuthorizationCodeInstalledApp googleAuthenticator;

    private final NetHttpTransport httpTransport;

    @Getter
    private final String userId;

    private GoogleDriveClient client;

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
        this.client = new Builder(httpTransport, GSON_FACTORY, googleCredentials)
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

    public StorageQuota storageQuota() {
        return client
            .about()
            .get()
            .setFields(STORAGE_QUOTA_FIELDS)
            .execute()
            .getStorageQuota();
    }

    @Override
    public Stream<File> stream(String query) {
        List<File> files = client
            .files()
            .list()
            .setSpaces(DRIVE)
            .setSupportsAllDrives(true)
            .setIncludeItemsFromAllDrives(true)
            .setQ(query)
            .setFields(FILES_FIELDS)
            .execute()
            .getFiles();
        files.forEach(this::populateFile);

        return files.stream();
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
            cache.cacheFile(path, populateFile(file));
        }
        return isFileCorrespondingToPath;
    }

    private boolean isRootPath(Path path) {
        return path == null || path.getFileName().toString().equals(userId);
    }

    private boolean isRootFile(File file) {
        return isEmpty(file.getParents());
    }

    private File populateFile(File file) {
        file.put(PROPERTY_TOTAL_SPACE, getTotalSpace());
        file.put(PROPERTY_FREE_SPACE, getFreeSpace());
        return file;
    }

    public File findFilesById(String fileId) {
        return client
            .files()
            .get(fileId)
            .setFields(FILE_FIELDS)
            .setSupportsAllDrives(true)
            .execute();
    }

    public List<File> findFilesByName(String fileName) {
        return client
            .files()
            .list()
            .setSpaces(DRIVE)
            .setQ(QUERY_BY_NAME.formatted(fileName))
            .setFields(FILES_FIELDS)
            .execute()
            .getFiles();
    }

    @Override
    public File save(File fileToSave) {
        return client
            .files()
            .create(fileToSave)
            .setFields(FILE_FIELDS)
            .execute();
    }

    @Override
    public boolean delete(File fileToDelete, boolean moveToTrash) {
//        if (moveToTrash) {
//            client
//                .files()
//                .update(fileToDelete.getId(), new File().setTrashed(true))
//                .execute();
//        } else {
//            client
//                .files()
//                .delete(fileToDelete.getId())
//                .execute();
//        }
        return true;
    }

    @Override
    public InputStream inputStream(String fileId) {
        return client
            .files()
            .get(fileId)
            .executeMediaAsInputStream();
    }

    @Override
    public OutputStream outputStream(File file, String query, boolean append) {
        return client
            .files()
            //TODO: getResumableUpload(file) when append == true
            .createResumableUpload(file)
            .setUploadHeaders()
            .executeAndGetOutputStream(this);
    }

    public void uploadBytesToFile(final File fileToUpdate, final byte[] bytes) {
        client
            .files()
            .resumeUpload(fileToUpdate, bytes)
            .setResumeUploadHeaders()
            .setThrowExceptionOnExecuteError(false)
            .execute();
    }

    @Override
    public void close() {
    }
}
