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

@Getter
@Setter
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AthleteStatistic {

    private Integer count;

    private Double distance;

    @JsonProperty("moving_time")
    private Long movingTime;

    @JsonProperty("elapsed_time")
    private Long elapsedTime;

    @JsonProperty("elevation_gain")
    private Double elevationGain;

    @JsonProperty("achievement_count")
    private Long achievementCount;

    @JsonProperty("biggest_distance")
    private Double biggestDistance;

    @JsonProperty("biggest_elevation_gain")
    private Double biggestElevationGain;
}
