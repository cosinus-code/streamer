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

package org.cosinus.streamer.strava.activity.gpx;

import io.jenetics.jpx.GPX;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.IntegerValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;
import org.cosinus.streamer.strava.model.ActivityStreams;
import org.cosinus.swing.file.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.strava.model.ActivityStreamType.*;

public class StravaActivityGpxStreamer extends StravaStreamer<byte[]> implements BinaryStreamer {

    public static final String GPX_TYPE = "gpx";

    public static final String GPX_ICON_NAME = "binary";

    @Autowired
    protected FileHandler fileHandler;

    private final StravaActivityStreamer stravaActivityStreamer;

    private final long stravaActivityId;

    private ActivityStreams activityStreams;

    public StravaActivityGpxStreamer(final StravaActivityStreamer stravaActivityStreamer) {
        super(stravaActivityStreamer.getStravaUserStreamer());
        this.stravaActivityStreamer = stravaActivityStreamer;
        this.stravaActivityId = stravaActivityStreamer.getActivity().getId();
    }

    @Override
    public String getName() {
        return stravaActivityId + "." + GPX_TYPE;
    }

    @Override
    public String getType() {
        return GPX_TYPE;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityStreamer;
    }

    @Override
    public String getIconName() {
        return GPX_ICON_NAME;
    }

    public ActivityStreams getActivityStreams() {
        if (activityStreams == null) {
            activityStreams = invokeStravaClient(stravaClient ->
                stravaClient.getActivityStreams(
                    stravaActivityId,
                    new String[]{
                        TIME.getName(),
                        LATITUDE_LONGITUDE.getName(),
                        ALTITUDE.getName()
                    },
                    true));
        }
        return activityStreams;
    }

    @Override
    public InputStream inputStream() {
        ActivityStreams streams = getActivityStreams();

        List<Integer> timeStream = streams.getDataStream(TIME);
        List<List<Double>> latitudeLongitudeStream = streams.getDataStream(LATITUDE_LONGITUDE);
        List<Double> altitudeStream = streams.getDataStream(ALTITUDE);

        long start = stravaActivityStreamer.getStartDate();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            GPX.Writer.DEFAULT.write(GPX.builder()
                .addTrack(track -> track
                    .addSegment(segment -> range(0, latitudeLongitudeStream.size())
                        .forEach(index -> segment.addPoint(p -> p
                            .lat(latitudeLongitudeStream.get(index).get(0))
                            .lon(latitudeLongitudeStream.get(index).get(1))
                            .ele(index < altitudeStream.size() ? altitudeStream.get(index) : 0d)
                            .time(start + (index < timeStream.size() ? timeStream.get(index) : 0L))
                        ))))
                .build(), output);

            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return null;
    }


    @Override
    public void reset() {
        details = asList(
            new TextValue(getName()),
            new IntegerValue(null)
        );
    }

    @Override
    public String getDescription() {
        return fileHandler.getTypeDescription(getPath(), false)
            .orElse("");
    }}
