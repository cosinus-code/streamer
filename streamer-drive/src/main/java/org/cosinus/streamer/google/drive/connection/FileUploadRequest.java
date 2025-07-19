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

import com.google.api.client.http.HttpRequest;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.UncheckedIOException;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveConnection.HEADER_CONTENT_RANGE;
import static org.cosinus.streamer.google.drive.connection.GoogleDriveConnection.PROPERTY_TOTAL_TO_UPLOAD;

public class FileUploadRequest {

    private final HttpRequest request;

    private final File fileToUpdate;

    private final int bytesCount;

    private long bytesCountToUpload;

    private long currentUploadedBytesCount;

    public FileUploadRequest(final HttpRequest request, final File fileToUpdate, final int bytesCount) throws IOException {
        this.request = request;
        this.fileToUpdate = fileToUpdate;
        this.bytesCount = bytesCount;
    }

    public FileUploadRequest setContentLength(long bytesCountToUpload) {
        request.getHeaders().setContentLength(bytesCountToUpload);
        this.bytesCountToUpload = bytesCountToUpload;
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

    public FileUploadRequest setResumeUploadHeaders() {
        long totalBytesCountToUpload = ofNullable(fileToUpdate.get(PROPERTY_TOTAL_TO_UPLOAD))
            .map(Object::toString)
            .map(Long::parseLong)
            .orElseThrow(() -> new GoogleDriveException("Unknown upload size for file: " + fileToUpdate.getName()));

        currentUploadedBytesCount = ofNullable(fileToUpdate.getSize())
            .orElse(0L);

        long bytesCountToUpload = currentUploadedBytesCount + bytesCount > totalBytesCountToUpload ?
            totalBytesCountToUpload - currentUploadedBytesCount :
            bytesCount;
        setContentLength(bytesCountToUpload);

        String contentRange = HEADER_CONTENT_RANGE.formatted(
            currentUploadedBytesCount,
            currentUploadedBytesCount + bytesCountToUpload - 1,
            totalBytesCountToUpload);
        setContentRange(contentRange);

        return this;
    }

    public void execute() {
        try (FileUploadResponse response = new FileUploadResponse(request.execute())) {
            if (response.isSuccessStatusCode()) {
                return;
            }

            if (response.getResponseStatusCode() != 308) {
                throw new GoogleDriveException("Failed to upload content for file: %s. Status code: %d"
                    .formatted(fileToUpdate.getName(), response.getResponseStatusCode()));
            }

            long bytesCountReceivedByServer = ofNullable(response.getResponseRange())
                .map(rangeHeader -> rangeHeader.substring(rangeHeader.indexOf('-') + 1))
                .map(Long::parseLong)
                .map(range -> range + 1)
                .orElse(-1L);

            if (bytesCountReceivedByServer >= 0 && bytesCountToUpload > bytesCountReceivedByServer) {
                throw new GoogleDriveException("The server received less bytes than expected: %d received but %d was sent"
                    .formatted(bytesCountReceivedByServer, bytesCountToUpload));
            }
            fileToUpdate.setSize(currentUploadedBytesCount + bytesCountToUpload);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
