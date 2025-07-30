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

import lombok.Getter;
import org.cosinus.streamer.strava.model.AthleteStatistic;
import org.cosinus.streamer.strava.model.AthleteStatistics;

import java.util.function.Function;

@Getter
public enum StravaStatisticsType {
    RECENT_RIDE_STATISTICS("recent-ride-statistics", AthleteStatistics::getRecentRideStatistics),
    YEAR_TO_DATE_RIDE_STATISTICS("year-to-date-ride-statistics", AthleteStatistics::getYearToDateRideStatistics),
    ALL_RIDE_STATISTICS("all-ride-statistics", AthleteStatistics::getAllRideStatistics),
    RECENT_RUN_STATISTICS("recent-run-statistics", AthleteStatistics::getRecentRunStatistics),
    YEAR_TO_DATE_RUN_STATISTICS("year-to-date-run-statistics", AthleteStatistics::getYearToDateRunStatistics),
    ALL_RUN_STATISTICS("all-run-statistics", AthleteStatistics::getAllRunStatistics),
    RECENT_SWIM_STATISTICS("recent-swim-statistics", AthleteStatistics::getRecentSwimStatistics),
    YEAR_TO_DATE_SWIM_STATISTICS("year-to-date-swim-statistics", AthleteStatistics::getYearToDateSwimStatistics),
    ALL_SWIM_STATISTICS("all-swim-statistics", AthleteStatistics::getAllSwimStatistics),
    RECENT_ACTIVITIES_STATISTICS("recent-activities-statistics", AthleteStatistics::getRecentActivitiesStatistics),
    YEAR_TO_DATE_ACTIVITIES_STATISTICS("year-to-date-activities-statistics", AthleteStatistics::getYearToDateActivitiesStatistics),
    ALL_ACTIVITIES_STATISTICS("all-activities-statistics", AthleteStatistics::getAllActivitiesStatistics);

    private final String key;

    private final Function<AthleteStatistics, AthleteStatistic> statisticsSupplier;

    StravaStatisticsType(final String key,
                         final Function<AthleteStatistics, AthleteStatistic> statisticsSupplier) {
        this.key = key;
        this.statisticsSupplier = statisticsSupplier;
    }

}
