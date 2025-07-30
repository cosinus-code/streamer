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

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityLap {

    private long id;

    @JsonProperty("resource_state")
    private int resourceState;

    private String name;

    @JsonProperty("elapsed_time")
    private long elapsedTime;

    @JsonProperty("moving_time")
    private long movingTime;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("start_date_local")
    private LocalDateTime startDateLocal;

    private Double distance;

    @JsonProperty("start_index")
    private Integer startIndex;

    @JsonProperty("end_index")
    private Integer endIndex;

    @JsonProperty("total_elevation_gain")
    private Double totalElevationGain;

    @JsonProperty("average_speed")
    private Double averageSpeed;

    @JsonProperty("max_speed")
    private Double maxSpeed;

    @JsonProperty("average_cadence")
    private Double averageCadence;

    @JsonProperty("device_watts")
    private Boolean deviceWatts;

    @JsonProperty("average_watts")
    private Double averageWatts;

    @JsonProperty("lap_index")
    private Integer lapIndex;

    private Integer split;
}
