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
        try (InputStream input = super.inputStream()) {
            return transform(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Transform the source content: read it, then write it in the target format.
     * <p>
     * Override it to handle a source which needs no transformation (e.g. already in the target format).
     *
     * @param input the source content, closed by the caller
     * @return the transformed content
     * @throws IOException if the source cannot be read or transformed
     */
    protected InputStream transform(final InputStream input) throws IOException {
        T source = read(input);
        if (source == null) {
            throw new IOException("Unsupported or unreadable source");
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(source, output);
        return new ByteArrayInputStream(output.toByteArray());
    }

    protected abstract T read(InputStream input) throws IOException;

    protected abstract void write(T source, OutputStream output) throws IOException;
}
