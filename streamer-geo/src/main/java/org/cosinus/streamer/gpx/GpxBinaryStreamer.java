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

package org.cosinus.streamer.gpx;

import io.jenetics.jpx.GPX;
import org.cosinus.streamer.api.BinaryStreamerDelegate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class GpxBinaryStreamer extends BinaryStreamerDelegate {

    private final GpxStreamer gpxStreamer;

    public GpxBinaryStreamer(final GpxStreamer gpxStreamer) {
        super(gpxStreamer.binaryStreamer());
        this.gpxStreamer = gpxStreamer;
    }

    @Override
    public InputStream inputStream() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            GPX.Writer.DEFAULT.write(gpxStreamer.gpx(), output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
