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

package org.cosinus.streamer.api.stream.pipeline.binary;

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

import static java.util.Optional.ofNullable;

public class BinaryStreamConsumer implements StreamConsumer<byte[]> {

    protected final OutputStream outputStream;

    public BinaryStreamConsumer(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void accept(byte[] bytes) {
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<String> checksum() {
        return ofNullable(outputStream)
            .filter(output -> CheckedOutputStream.class.isAssignableFrom(output.getClass()))
            .map(CheckedOutputStream.class::cast)
            .map(CheckedOutputStream::getChecksum)
            .map(Checksum::getValue)
            .map(Objects::toString);

    }

    @Override
    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
    }

}
