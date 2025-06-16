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
package org.cosinus.streamer.api.stream.consumer;

import org.cosinus.streamer.api.error.StreamerException;

import java.io.*;

public class TemporaryFileOutputStream extends FilterOutputStream {

    private final File file;

    private final File tempFile;

    public TemporaryFileOutputStream(final File file, final TemporaryFileStrategy temporaryFileStrategy)
        throws FileNotFoundException {

        super(new FileOutputStream(temporaryFileStrategy.getFile(file), false));
        this.file = file;
        this.tempFile = temporaryFileStrategy.getFile(file);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    public void afterClose(boolean failed) {
        if (!failed && !(file.delete() && tempFile.renameTo(file))) {
            throw new StreamerException("Failed to finalize save for: " + file);
        }
    }

}
