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

package org.cosinus.streamer.strava.activity.laps;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.ElevationValue;
import org.cosinus.streamer.api.value.PaceValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.model.ActivityLap;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.cosinus.streamer.strava.activity.laps.StravaActivityLapsJsonStreamer.LAPS_ICON_NAME;

public class StravaActivityLapStreamer extends StravaJsonStreamer {

    protected static final int DETAILS_INDEX_PACE = 1;

    protected static final int DETAILS_INDEX_ELEVATION = 2;

    @Autowired
    private FormatHandler formatHandler;

    @Autowired
    private Translator translator;

    private final StravaActivityLapsStreamer stravaActivityLapsStreamer;

    private final ActivityLap activityLap;

    public StravaActivityLapStreamer(final StravaActivityLapsStreamer stravaActivityLapsStreamer,
                                     final ActivityLap activityLap) {
        super(stravaActivityLapsStreamer.binaryStreamer().getStravaUserStreamer());
        this.stravaActivityLapsStreamer = stravaActivityLapsStreamer;
        this.activityLap = activityLap;
    }

    @Override
    public String getName() {
        return activityLap.getName();
    }

    @Override
    public String getIconName() {
        return LAPS_ICON_NAME;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityLapsStreamer;
    }

    @Override
    public Object getSource() {
        return activityLap;
    }

    protected Double getPace() {
        return ofNullable(activityLap.getAverageSpeed())
            .map(averageSpeed -> 1000 / (60 * averageSpeed))
            .orElse(null);
    }

    @Override
    public void reset() {
        details = asList(
            new TextValue(formatHandler.formatIndexAsString(
                activityLap.getLapIndex(),
                stravaActivityLapsStreamer.getLapsCount())),
            new PaceValue(getPace()),
            new ElevationValue(activityLap.getTotalElevationGain())
        );
    }

    @Override
    public String getDescription() {
        return Stream.of(
                ofNullable(details().get(DETAILS_INDEX_PACE))
                    .map(distance -> translator.translate("strava-activity-pace", distance))
                    .orElse(null),
                ofNullable(details().get(DETAILS_INDEX_ELEVATION))
                    .map(elevation -> translator.translate("strava-activity-elevation", elevation))
                    .orElse(null))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(not(String::isBlank))
            .collect(joining(", "));
    }
}
