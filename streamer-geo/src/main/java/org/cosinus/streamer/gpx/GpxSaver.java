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
import io.jenetics.jpx.Latitude;
import io.jenetics.jpx.Length;
import io.jenetics.jpx.Longitude;
import io.jenetics.jpx.WayPoint;
import org.cosinus.stream.consumer.StreamConsumer;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                    .listMap(this::updatePointsIfNeeded)
                    .filter(Objects::nonNull)
                    .build())
                .filter(segment -> !segment.getPoints().isEmpty())
                .build())
            .filter(track -> !track.getSegments().isEmpty())
            .build()
            .build(), output);
        output.close();
    }

    private List<WayPoint> updatePointsIfNeeded(List<WayPoint> points) {
        return points
            .stream()
            .map(this::updatePointIfNeeded)
            .filter(Objects::nonNull)
            .toList();
    }

    private WayPoint updatePointIfNeeded(WayPoint point) {
        return ofNullable(pointsMap.get(getKey(point)))
            .map(gpxPoint -> updatePoint(point, gpxPoint))
            .orElse(null);
    }


    private WayPoint updatePoint(WayPoint point, GpxPoint gpxPoint) {
        WayPoint.Builder builder = point.toBuilder();

        gpxPoint.init();
        gpxPoint.getDateDetail(0)
            .map(Date::toInstant)
            .ifPresent(builder::time);

        gpxPoint.getDoubleDetail(1)
            .map(Latitude::ofDegrees)
            .ifPresent(builder::lat);

        gpxPoint.getDoubleDetail(2)
            .map(Longitude::ofDegrees)
            .ifPresent(builder::lon);

        gpxPoint.getDoubleDetail(3)
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
