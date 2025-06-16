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

import com.google.api.client.http.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;

import static org.cosinus.streamer.google.drive.connection.CreateResumableFileUpload.UPLOAD_URL;

public class FileUploadRequest implements AutoCloseable{

    private final HttpRequest request;

    private HttpResponse response;

    public FileUploadRequest(final Drive client, File fileToUpdate, final byte[] bytes) throws IOException {
        GenericUrl uploadUrl = new GenericUrl((String) fileToUpdate.get(UPLOAD_URL));
        HttpContent content = new ByteArrayContent(fileToUpdate.getMimeType(), bytes);
        this.request = client.getRequestFactory().buildPutRequest(uploadUrl, content);
    }

    public FileUploadRequest setContentLength(Long contentLength) {
        request.getHeaders().setContentLength(contentLength);
        return this;
    }

    public FileUploadRequest setContentRange(String contentRange) {
        request.getHeaders().setContentRange(contentRange);
        return this;
    }

    public FileUploadRequest setThrowExceptionOnExecuteError(boolean throwExceptionOnExecuteError) {
        request.setThrowExceptionOnExecuteError(throwExceptionOnExecuteError);
        return this;
    }

    public FileUploadRequest execute() throws IOException {
        response = request.execute();
        return this;
    }

    public boolean isSuccessStatusCode() {
        return response.isSuccessStatusCode();
    }

    public int getResponseStatusCode() {
        return response.getStatusCode();
    }

    public String getResponseLocation() {
        return response.getHeaders().getLocation();
    }

    public String getResponseRange() {
        return response.getHeaders().getRange();
    }

    @Override
    public void close() {
        if (response != null) {
            try {
                response.disconnect();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
