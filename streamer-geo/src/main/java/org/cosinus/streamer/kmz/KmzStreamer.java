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
package org.cosinus.streamer.kmz;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.gpx.GpxStreamer;
import org.cosinus.swing.xml.Xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.kmz.KmzExpander.KMZ_PROTOCOL;

public class KmzStreamer extends GpxStreamer {

    public KmzStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected GPX gpx() {
        if (gpx == null) {
            try (ZipFile zip = new ZipFile(getPath().toFile())) {
                ZipEntry zipEntry = zip.stream()
                    .filter(entry -> entry.getName().endsWith(".kml"))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("No KML in KMZ"));

                GPX.Builder gpxBuilder = GPX.builder();
                parseKml(zip.getInputStream(zipEntry))
                    .forEach(trackPoints -> gpxBuilder
                        .addTrack(track -> track
                            .addSegment(segment -> segment
                                .points(trackPoints))));
                gpx = gpxBuilder.build();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return gpx;
    }

    private List<List<WayPoint>> parseKml(InputStream input) {
        Xml xml = new Xml(input);
        return xml.elements("LineString", "Point")
            .map(line -> xml.getTextSubElement(line, "coordinates"))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::parseCoordinates)
            .toList();
    }

    private List<WayPoint> parseCoordinates(String coordinates) {
        return ofNullable(coordinates)
            .map(String::trim)
            .map(text -> text.split("\\s+"))
            .stream()
            .flatMap(Arrays::stream)
            .map(part -> part.split(","))
            .filter(parts -> parts.length > 1)
            .map(vals -> {
                double longitude = Double.parseDouble(vals[0]);
                double latitude = Double.parseDouble(vals[1]);
                double elevation = vals.length > 2 ? Double.parseDouble(vals[2]) : 0;
                return WayPoint.builder()
                    .lat(latitude)
                    .lon(longitude)
                    .ele(elevation)
                    .build();
            })
            .toList();
    }

    @Override
    public String getProtocol() {
        return KMZ_PROTOCOL;
    }
}
