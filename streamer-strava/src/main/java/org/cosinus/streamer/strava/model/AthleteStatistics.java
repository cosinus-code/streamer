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

package org.cosinus.streamer.strava.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Getter
@Setter
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AthleteStatistics {

    @JsonProperty(value = "biggest_ride_distance", access = WRITE_ONLY)
    private Double biggestRideDistance;

    @JsonProperty(value = "biggest_climb_elevation_gain", access = WRITE_ONLY)
    private Double biggestClimbElevationGain;

    @JsonProperty("recent_ride_totals")
    private AthleteStatistic recentRideStatistics;

    @JsonProperty("all_ride_totals")
    private AthleteStatistic allRideStatistics;

    @JsonProperty("recent_run_totals")
    private AthleteStatistic recentRunStatistics;

    @JsonProperty("all_run_totals")
    private AthleteStatistic allRunStatistics;

    @JsonProperty("recent_swim_totals")
    private AthleteStatistic recentSwimStatistics;

    @JsonProperty("all_swim_totals")
    private AthleteStatistic allSwimStatistics;

    @JsonProperty("ytd_ride_totals")
    private AthleteStatistic yearToDateRideStatistics;

    @JsonProperty("ytd_run_totals")
    private AthleteStatistic yearToDateRunStatistics;

    @JsonProperty("ytd_swim_totals")
    private AthleteStatistic yearToDateSwimStatistics;

    @JsonProperty("recent_activities_totals")
    private AthleteStatistic recentActivitiesStatistics;

    @JsonProperty("ytd_activities_totals")
    private AthleteStatistic yearToDateActivitiesStatistics;

    @JsonProperty("all_activities_totals")
    private AthleteStatistic allActivitiesStatistics;
}
