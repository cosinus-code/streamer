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
package org.cosinus.streamer.gpx;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.streamer.gpx.GpxExpander.GPX_PROTOCOL;

public class GpxStreamer extends ExpandedStreamer<GpxPoint> implements Streamer<GpxPoint> {

    public GpxStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    public Stream<GpxPoint> stream() {
        try {
            InputStream input = binaryStreamer.inputStream();
            return GPX.Reader.DEFAULT.read(input)
                .tracks()
                .flatMap(Track::segments)
                .flatMap(TrackSegment::points)
                .map(point -> new GpxPoint(this, point))
                .onClose(() -> {
                    try {
                        input.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<GpxPoint> find(String path) {
        return Optional.empty();
    }

    @Override
    public void finishLoading() {

    }

    @Override
    public String getProtocol() {
        return GPX_PROTOCOL;
    }
}
