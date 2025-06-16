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

import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveRequest;
import com.google.api.services.drive.model.File;

import java.io.IOException;

import static com.google.api.client.http.HttpMethods.POST;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveConnection.PROPERTY_UPLOAD_TYPE;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveUploadType.RESUMABLE;

public class CreateResumableFileUpload extends DriveRequest<File> {

    public static final String REST_PATH = "files";

    public static final String UPLOAD_PATH = "/upload/";

    public static final String UPLOAD_URL = "uploadUrl";

    private File file;

    private String uploadUrl;

    public CreateResumableFileUpload(Drive client, File file) {
        super(client, POST,  UPLOAD_PATH + client.getServicePath() + REST_PATH, file, File.class);
        set(PROPERTY_UPLOAD_TYPE, RESUMABLE.getValue());
        this.file = file;
    }

    @Override
    public HttpResponse executeUnparsed() throws IOException {
        HttpResponse response = super.executeUnparsed();
        uploadUrl = response.getHeaders().getLocation();
        return response;
    }

    @Override
    public File execute() throws IOException {
        executeUnparsed();
        file.put(UPLOAD_URL, uploadUrl);
        return file;
    }
}
