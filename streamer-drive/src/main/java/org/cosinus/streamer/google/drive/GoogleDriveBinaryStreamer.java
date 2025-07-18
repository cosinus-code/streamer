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
import org.cosinus.streamer.api.remote.RemoteBinaryStreamer;
import org.cosinus.streamer.google.drive.connection.GoogleDriveConnection;
import org.cosinus.streamer.google.drive.connection.GoogleDriveOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static java.util.Optional.ofNullable;

public class GoogleDriveBinaryStreamer
    extends GoogleDriveStreamer<byte[]>
    implements RemoteBinaryStreamer<File, GoogleDriveConnection> {

    private GoogleDriveConnection inputStreamConnection;

    private GoogleDriveConnection outputStreamConnection;

    public GoogleDriveBinaryStreamer(File file, Path path, String userId) {
        super(file, path, userId);
    }

    @Override
    public String getStreamQuery() {
        return file.getId();
    }

    @Override
    public InputStream getInputStream(GoogleDriveConnection connection) {
        InputStream inputStream = RemoteBinaryStreamer.super.getInputStream(connection);
        this.inputStreamConnection = connection;
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream(GoogleDriveConnection connection, boolean append) {
        OutputStream outputStream = new GoogleDriveOutputStream(file, connection, append);
        this.outputStreamConnection = connection;
        return outputStream;
    }

    @Override
    public void finalizeStreaming() {
        ofNullable(inputStreamConnection)
            .ifPresent(googleDriveConnectionPool::returnConnection);
        ofNullable(outputStreamConnection)
            .ifPresent(googleDriveConnectionPool::returnConnection);
    }
}
