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

package org.cosinus.streamer.api.transform;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.BinaryStreamerDelegate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public abstract class TransformBinaryStreamer<T> extends BinaryStreamerDelegate {

    public TransformBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    public InputStream inputStream() {
        try (InputStream input = super.inputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            T source = read(input);
            if (source == null) {
                throw new IOException("Unsupported or unreadable source");
            }

            write(source, output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected abstract T read(InputStream input) throws IOException;

    protected abstract void write(T source, OutputStream output) throws IOException;
}
