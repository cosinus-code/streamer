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

import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.OutputStream;

import static org.apache.commons.lang3.ArrayUtils.subarray;

public class GoogleDriveOutputStream extends OutputStream {

    private final GoogleDriveConnection connection;

    private final File file;

    public GoogleDriveOutputStream(final GoogleDriveConnection connection,
                                   final File file) {
        this.connection = connection;
        this.file = file;
    }

    @Override
    public void write(int b) throws IOException {
        uploadBytes(new byte[]{(byte) b});
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        uploadBytes(bytes);
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        uploadBytes(subarray(bytes, off, off + len));
    }

    protected void uploadBytes(byte[] bytes) throws IOException {
        connection.uploadBytesToFile(file, bytes);
    }
}
