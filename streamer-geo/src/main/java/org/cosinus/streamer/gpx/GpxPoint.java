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

import io.jenetics.jpx.WayPoint;
import org.cosinus.streamer.api.Streamable;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.cosinus.streamer.gpx.GpxExpander.GPX_PROTOCOL;

public class GpxPoint implements Streamable {

    private final GpxStreamer gpxStreamer;

    private final WayPoint point;

    public GpxPoint(final GpxStreamer gpxStreamer, final WayPoint point) {
        this.gpxStreamer = gpxStreamer;
        this.point = point;
    }

    @Override
    public String getName() {
        return point.toString();
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
}
