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

package org.cosinus.streamer.strava.statististics;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.value.*;
import org.cosinus.streamer.strava.model.AthleteStatistic;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.cosinus.streamer.strava.statististics.StravaStatisticsJsonStreamer.STATISTICS_ICON_NAME;

public class StravaStatisticStreamer implements Streamable {

    protected static final int DETAILS_INDEX_COUNT = 1;

    protected static final int DETAILS_INDEX_DISTANCE = 2;

    protected static final int DETAILS_INDEX_ELEVATION = 5;

    final StravaStatisticsStreamer stravaStatisticsStreamer;

    private final String statisticName;

    private final AthleteStatistic athleteStatistic;

    private List<Value> details;

    public StravaStatisticStreamer(final StravaStatisticsStreamer stravaStatisticsStreamer,
                                   final String statisticName,
                                   final AthleteStatistic athleteStatistic) {
        this.stravaStatisticsStreamer = stravaStatisticsStreamer;
        this.statisticName = statisticName;
        this.athleteStatistic = athleteStatistic;
    }

    @Override
    public String getName() {
        return statisticName;
    }

    @Override
    public String getDescription() {
        return Stream.of(DETAILS_INDEX_COUNT, DETAILS_INDEX_DISTANCE, DETAILS_INDEX_ELEVATION)
            .map(detailIndex -> ofNullable(details().get(detailIndex))
                .map(detailValue -> stravaStatisticsStreamer.detailNames().get(detailIndex) + ": " + detailValue))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(joining(", "));
    }

    @Override
    public String getIconName() {
        return STATISTICS_ICON_NAME;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public Streamable getParent() {
        return stravaStatisticsStreamer;
    }

    @Override
    public List<Value> details() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new IntegerValue(athleteStatistic.getCount()),
                new DistanceValue(athleteStatistic.getDistance()),
                new DurationValue(athleteStatistic.getMovingTime()),
                new DurationValue(athleteStatistic.getElapsedTime()),
                new ElevationValue(athleteStatistic.getElevationGain()),
                new LongValue(athleteStatistic.getAchievementCount()),
                new DistanceValue(athleteStatistic.getBiggestDistance()),
                new ElevationValue(athleteStatistic.getBiggestElevationGain())
            );
        }
        return details;
    }
}
