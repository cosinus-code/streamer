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

import io.jenetics.jpx.Length;
import io.jenetics.jpx.WayPoint;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.value.DateValue;
import org.cosinus.streamer.api.value.DoubleValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.cosinus.streamer.gpx.GpxExpander.GPX_PROTOCOL;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FILE;

public class GpxPoint implements Streamable {

    private final GpxStreamer gpxStreamer;

    private final String id;

    private WayPoint point;

    protected List<Value> details;

    private final String name;

    private final long lastModified;

    public GpxPoint(final GpxStreamer gpxStreamer, String id, final WayPoint point) {
        this.gpxStreamer = gpxStreamer;
        this.id = id;
        this.point = point;
        this.name = point.getName().orElseGet(point::toString);
        this.lastModified = point.getTime()
            .map(Instant::toEpochMilli)
            .orElse(0L);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Path getPath() {
        return Paths.get(getName());
    }

    @Override
    public String getProtocol() {
        return GPX_PROTOCOL;
    }

    @Override
    public Streamable getParent() {
        return gpxStreamer;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public List<Value> details() {
        init();
        return details;
    }

    @Override
    public void init() {
        if (details == null) {
            details = asList(
                new DateValue(point.getTime()
                    .map(Instant::toEpochMilli)
                    .orElse(null)),
                new TextValue(format("%.6f", point.getLatitude().doubleValue())),
                new TextValue(format("%.6f", point.getLongitude().doubleValue())),
                new TextValue(format("%.2f", point.getElevation()
                    .map(Length::doubleValue)
                    .orElse(null))));
        }
    }

    @Override
    public boolean canUpdateDetail(int detailIndex) {
        return detailIndex > 0;
    }

    @Override
    public void save() {
    }

    public WayPoint getPoint() {
        return point;
    }

    @Override
    public String getIconName() {
        return ICON_FILE;
    }
}
