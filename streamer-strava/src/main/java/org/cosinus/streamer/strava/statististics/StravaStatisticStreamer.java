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

import static java.util.Arrays.asList;
import static org.cosinus.streamer.strava.statististics.StravaStatisticsJsonStreamer.STATISTICS_ICON_NAME;

public class StravaStatisticStreamer implements Streamable {

    private final String statisticName;

    private final AthleteStatistic athleteStatistic;

    private List<Value> details;

    public StravaStatisticStreamer(final String statisticName,
                                   final AthleteStatistic athleteStatistic) {
        this.statisticName = statisticName;
        this.athleteStatistic = athleteStatistic;
    }

    @Override
    public String getName() {
        return statisticName;
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
        return null;
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
