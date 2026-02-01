/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.google.drive.connection;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;

import java.io.IOException;
import java.io.InputStream;

import static org.cosinus.streamer.google.drive.connection.CreateResumableFileUpload.UPLOAD_URL;

public class GoogleDriveClient extends Drive {

    public GoogleDriveClient(final HttpTransport transport,
                             final JsonFactory jsonFactory,
                             final HttpRequestInitializer httpRequestInitializer) {
        super(transport, jsonFactory, httpRequestInitializer);
    }

    @Override
    public GoogleDriveFiles files() {
        return new GoogleDriveFiles();
    }

    @Override
    public GoogleDrivePermissions permissions() {
        return new GoogleDrivePermissions();
    }

    @Override
    public GoogleDriveAbout about() {
        return new GoogleDriveAbout();
    }

    @Override
    protected void initialize(AbstractGoogleClientRequest<?> httpClientRequest) {
        try {
            super.initialize(httpClientRequest);
        } catch (IOException e) {
            throw new GoogleDriveException(e);
        }
    }

    public class GoogleDrivePermissions extends Permissions {

        @Override
        public GoogleDrivePermissionsList list(String fileId) {
            GoogleDrivePermissionsList list = new GoogleDrivePermissionsList(fileId);
            initialize(list);
            return list;
        }

        @Override
        public GoogleDrivePermissionGet get(String fileId, String permissionId) {
            GoogleDrivePermissionGet get = new GoogleDrivePermissionGet(fileId, permissionId);
            initialize(get);
            return get;
        }

        @Override
        public GoogleDrivePermissionCreate create(String fileId, Permission content) {
            GoogleDrivePermissionCreate create = new GoogleDrivePermissionCreate(fileId, content);
            initialize(create);
            return create;
        }

        @Override
        public GoogleDrivePermissionUpdate update(String fileId, String permissionId, Permission content) {
            GoogleDrivePermissionUpdate update = new GoogleDrivePermissionUpdate(fileId, permissionId, content);
            initialize(update);
            return update;
        }

        @Override
        public GoogleDrivePermissionDelete delete(String fileId, String permissionId) {
            GoogleDrivePermissionDelete delete = new GoogleDrivePermissionDelete(fileId, permissionId);
            initialize(delete);
            return delete;
        }

        public class GoogleDrivePermissionsList extends Permissions.List {
            protected GoogleDrivePermissionsList(String fileId) {
                super(fileId);
            }

            @Override
            public GoogleDrivePermissionsList setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public PermissionList execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDrivePermissionGet extends Permissions.Get {
            protected GoogleDrivePermissionGet(String fileId, String permissionId) {
                super(fileId, permissionId);
            }

            @Override
            public GoogleDrivePermissionGet setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public Permission execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDrivePermissionCreate extends Permissions.Create {
            protected GoogleDrivePermissionCreate(String fileId, Permission content) {
                super(fileId, content);
            }

            @Override
            public GoogleDrivePermissionCreate setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public Permission execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDrivePermissionUpdate extends Permissions.Update {
            protected GoogleDrivePermissionUpdate(String fileId, String permissionId, Permission content) {
                super(fileId, permissionId, content);
            }

            @Override
            public GoogleDrivePermissionUpdate setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public Permission execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDrivePermissionDelete extends Permissions.Delete {
            protected GoogleDrivePermissionDelete(String fileId, String permissionId) {
                super(fileId, permissionId);
            }

            @Override
            public Void execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }
    }

    public class GoogleDriveFiles extends Files {

        @Override
        public GoogleDriveList list() {
            GoogleDriveList list = new GoogleDriveList();
            initialize(list);
            return list;
        }

        @Override
        public GoogleDriveCreate create(File content, AbstractInputStreamContent mediaContent) {
            GoogleDriveCreate create = new GoogleDriveCreate(content, mediaContent);
            initialize(create);
            return create;
        }

        @Override
        public GoogleDriveCreate create(File file) {
            GoogleDriveCreate create = new GoogleDriveCreate(file);
            initialize(create);
            return create;
        }

        public CreateResumableFileUpload createResumableUpload(File file) {
            CreateResumableFileUpload create = new CreateResumableFileUpload(GoogleDriveClient.this, file);
            initialize(create);
            return create;
        }

        @Override
        public GoogleDriveGet get(String fileId) {
            GoogleDriveGet get = new GoogleDriveGet(fileId);
            initialize(get);
            return get;
        }

        @Override
        public GoogleDriveUpdate update(final String fileId, final File content) {
            GoogleDriveUpdate update = new GoogleDriveUpdate(fileId, content);
            initialize(update);
            return update;
        }

        public FileUploadRequest resumeUpload(final File fileToUpdate, final byte[] bytes) {
            try {
                GenericUrl uploadUrl = new GenericUrl((String) fileToUpdate.get(UPLOAD_URL));
                HttpContent content = new ByteArrayContent(fileToUpdate.getMimeType(), bytes);
                HttpRequest request = getRequestFactory().buildPutRequest(uploadUrl, content);
                return new FileUploadRequest(request, fileToUpdate, bytes.length);
            } catch (IOException e) {
                throw new GoogleDriveException(e);
            }
        }

        @Override
        public GoogleDriveDelete delete(String fileId) {
            GoogleDriveDelete delete = new GoogleDriveDelete(fileId);
            initialize(delete);
            return delete;
        }

        public class GoogleDriveList extends List {
            @Override
            public GoogleDriveList setSpaces(String spaces) {
                super.setSpaces(spaces);
                return this;
            }

            @Override
            public GoogleDriveList setQ(String q) {
                super.setQ(q);
                return this;
            }

            @Override
            public GoogleDriveList setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public GoogleDriveList setSupportsAllDrives(Boolean supportsAllDrives) {
                super.setSupportsAllDrives(supportsAllDrives);
                return this;
            }

            @Override
            public GoogleDriveList setIncludeItemsFromAllDrives(Boolean includeItemsFromAllDrives) {
                super.setIncludeItemsFromAllDrives(includeItemsFromAllDrives);
                return this;
            }

            @Override
            public FileList execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDriveGet extends Get {

            protected GoogleDriveGet(String fileId) {
                super(fileId);
            }

            @Override
            public GoogleDriveGet setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public GoogleDriveGet setSupportsAllDrives(Boolean supportsAllDrives) {
                super.setSupportsAllDrives(supportsAllDrives);
                return this;
            }


            @Override
            public InputStream executeMediaAsInputStream() {
                try {
                    return super.executeMediaAsInputStream();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }

            @Override
            public File execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDriveCreate extends Create {

            protected GoogleDriveCreate(File content) {
                super(content);
            }

            protected GoogleDriveCreate(File content, AbstractInputStreamContent mediaContent) {
                super(content, mediaContent);
            }

            @Override
            public GoogleDriveCreate setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public File execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDriveUpdate extends Update {

            protected GoogleDriveUpdate(String fileId, File content) {
                super(fileId, content);
            }

            @Override
            public File execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }

        public class GoogleDriveDelete extends Delete {

            protected GoogleDriveDelete(String fileId) {
                super(fileId);
            }

            @Override
            public Void execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }
    }

    public class GoogleDriveAbout extends About {

        @Override
        public GoogleDriveGetAbout get() {
            GoogleDriveGetAbout result = new GoogleDriveGetAbout();
            initialize(result);
            return result;
        }

        public class GoogleDriveGetAbout extends Get {
            @Override
            public GoogleDriveGetAbout setFields(String fields) {
                super.setFields(fields);
                return this;
            }

            @Override
            public com.google.api.services.drive.model.About execute() {
                try {
                    return super.execute();
                } catch (IOException e) {
                    throw new GoogleDriveException(e);
                }
            }
        }
    }

    public static final class Builder
        extends com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient.Builder {

        public Builder(final HttpTransport transport,
                       final JsonFactory jsonFactory,
                       final HttpRequestInitializer httpRequestInitializer) {
            super(
                transport,
                jsonFactory,
                DEFAULT_ROOT_URL,
                DEFAULT_SERVICE_PATH,
                httpRequestInitializer,
                false);
            setBatchPath(DEFAULT_BATCH_PATH);
        }

        /**
         * Builds a new instance of {@link Drive}.
         */
        @Override
        public GoogleDriveClient build() {
            return new GoogleDriveClient(getTransport(), getJsonFactory(), getHttpRequestInitializer());
        }

        @Override
        public Builder setRootUrl(String rootUrl) {
            super.setRootUrl(rootUrl);
            return this;
        }

        @Override
        public Builder setServicePath(String servicePath) {
            super.setServicePath(servicePath);
            return this;
        }

        @Override
        public Builder setBatchPath(String batchPath) {
            super.setBatchPath(batchPath);
            return this;
        }

        @Override
        public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
            super.setHttpRequestInitializer(httpRequestInitializer);
            return this;
        }

        @Override
        public Builder setApplicationName(String applicationName) {
            super.setApplicationName(applicationName);
            return this;
        }

        @Override
        public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
            super.setSuppressPatternChecks(suppressPatternChecks);
            return this;
        }

        @Override
        public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
            super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
            return this;
        }

        @Override
        public Builder setSuppressAllChecks(boolean suppressAllChecks) {
            super.setSuppressAllChecks(suppressAllChecks);
            return this;
        }

        @Override
        public Builder setGoogleClientRequestInitializer(
            GoogleClientRequestInitializer googleClientRequestInitializer) {
            super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
            return this;
        }
    }
}
