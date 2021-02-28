/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.zip.stream;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipOutputStream;

/**
 * A {@link ZipOutputStream} fot a {@link ZipStreamEntry}
 */
public class ZipEntryOutputStream extends ZipOutputStream {

    private final ZipOutputStream output;

    public ZipEntryOutputStream(ZipOutputStream output,
                                ZipStreamEntry zipEntry) throws IOException {
        this(output, zipEntry, StandardCharsets.UTF_8);

    }

    public ZipEntryOutputStream(ZipOutputStream output,
                                ZipStreamEntry zipEntry,
                                Charset charset) throws IOException {
        super(output, charset);
        this.output = output;
        output.putNextEntry(zipEntry);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        output.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        output.closeEntry();
    }

    public void closeZipStream() throws IOException {
        output.close();
    }
}
