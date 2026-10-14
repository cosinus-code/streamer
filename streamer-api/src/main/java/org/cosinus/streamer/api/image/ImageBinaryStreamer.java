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

package org.cosinus.streamer.api.image;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.BinaryStreamerDelegate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static javax.imageio.ImageIO.read;
import static javax.imageio.ImageIO.write;

public class ImageBinaryStreamer extends BinaryStreamerDelegate {

    private final String imageFormat;

    public ImageBinaryStreamer(final BinaryStreamer binaryStreamer, final String imageFormat) {
        super(binaryStreamer);
        this.imageFormat = imageFormat;
    }

    @Override
    public InputStream inputStream() {
        try (InputStream input = super.inputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            write(read(input), imageFormat, output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
