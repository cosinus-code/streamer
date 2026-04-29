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
package org.cosinus.streamer.gpx;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.value.TranslatableName;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.cosinus.streamer.gpx.GpxExpander.GPX_PROTOCOL;

public class GpxStreamer extends ExpandedStreamer<GpxPoint> implements Streamer<GpxPoint> {

    protected static final String DETAIL_KEY_TIME = "time";
    protected static final String DETAIL_KEY_LATITUDE = "latitude";
    protected static final String DETAIL_KEY_LONGITUDE = "longitude";
    protected static final String DETAIL_KEY_ELEVATION = "elevation";

    protected final List<TranslatableName> detailNames;

    protected GPX gpx;

    public GpxStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_TIME,
            DETAIL_KEY_LATITUDE,
            DETAIL_KEY_LONGITUDE,
            DETAIL_KEY_ELEVATION
        );

    }

    @Override
    public Stream<GpxPoint> stream() {
        final AtomicLong index = new AtomicLong();
        return gpx()
            .tracks()
            .flatMap(Track::segments)
            .flatMap(TrackSegment::points)
            .map(point -> new GpxPoint(this, Long.toString(index.incrementAndGet()), point));
    }

    protected GPX gpx() {
        if (gpx == null) {
            try (InputStream input = binaryStreamer.inputStream()) {
                gpx = GPX.Reader.DEFAULT.read(input);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return gpx;
    }

    @Override
    public StreamConsumer<GpxPoint> streamConsumer() {
        return new GpxSaver(gpx(), binaryStreamer.outputStream(false));
    }

    @Override
    public String getProtocol() {
        return GPX_PROTOCOL;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
