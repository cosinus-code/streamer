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

import io.jenetics.jpx.*;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.value.DateValue;
import org.cosinus.streamer.api.value.DoubleValue;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jenetics.jpx.Length.Unit.METER;
import static java.lang.String.join;
import static java.util.Optional.ofNullable;

public class GpxSaver implements StreamConsumer<GpxPoint> {

    private final GPX gpx;

    private final OutputStream output;

    private final Map<String, GpxPoint> pointsMap;

    public GpxSaver(final GPX gpx, final OutputStream output) {
        this.gpx = gpx;
        this.output = output;
        this.pointsMap = new HashMap<>();
    }

    @Override
    public void accept(GpxPoint gpxPoint) {
        pointsMap.put(getKey(gpxPoint.getPoint()), gpxPoint);
    }

    @Override
    public void close() throws IOException {
        GPX.Writer.DEFAULT.write(gpx.toBuilder()
            .trackFilter()
            .map(track -> track.toBuilder()
                .map(segment -> segment.toBuilder()
                    .map(this::updatePoint)
                    .build())
                .build())
            .build()
            .build(), output);
    }

    private WayPoint updatePoint(WayPoint point) {
        return updatePoint(point, pointsMap.get(getKey(point)));
    }


    private WayPoint updatePoint(WayPoint point, GpxPoint gpxPoint) {
        WayPoint.Builder builder = point.toBuilder();

        gpxPoint.init();
        ofNullable(gpxPoint.details.get(0))
            .map(DateValue.class::cast)
            .map(DateValue::value)
            .map(Date::toInstant)
            .ifPresent(builder::time);

        ofNullable(gpxPoint.details.get(1))
            .map(DoubleValue.class::cast)
            .map(DoubleValue::value)
            .map(Latitude::ofDegrees)
            .ifPresent(builder::lat);

        ofNullable(gpxPoint.details.get(2))
            .map(DoubleValue.class::cast)
            .map(DoubleValue::value)
            .map(Longitude::ofDegrees)
            .ifPresent(builder::lon);

        ofNullable(gpxPoint.details.get(2))
            .map(DoubleValue.class::cast)
            .map(DoubleValue::value)
            .map(elevation -> Length.of(elevation, METER))
            .ifPresent(builder::ele);

        return builder.build();
    }

    private String getKey(WayPoint point) {
        return join("#",
            point.getTime()
                .map(Instant::toEpochMilli)
                .map(Object::toString)
                .orElse(""),
            point.getLatitude().toString(),
            point.getLongitude().toString(),
            point.getElevation()
                .map(Length::toString)
                .orElse(""));
    }
}
