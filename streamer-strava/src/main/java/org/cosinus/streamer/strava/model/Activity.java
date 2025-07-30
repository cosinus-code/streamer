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

import java.io.Serializable;
import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity implements Serializable {

    private Long id;

    private String name;

    private String description;

    private AthleteProfile athlete;

    private Double distance;

    private ActivityType type;

    @JsonProperty("sport_type")
    private SportType sportType;

    @JsonProperty("start_date")
    private Date startDate;

    @JsonProperty("start_date_local")
    private Date startDateLocal;

    private Map map;

    @JsonProperty("private")
    private boolean isPrivate;

    private boolean trainer;

    private boolean commute;

    private boolean manual;

    @JsonProperty("resource_state")
    private int resourceState;

    private Gear gear;

    @JsonProperty("moving_time")
    private long movingTime;

    @JsonProperty("elapsed_time")
    private long elapsedTime;

    @JsonProperty("total_elevation_gain")
    private long totalElevationGain;

    @JsonProperty("kudos_count")
    private long kudosCount;

    @JsonProperty("comment_count")
    private long commentCount;

    @JsonProperty("athlete_count")
    private long athleteCount;

    @JsonProperty("photo_count")
    private long photoCount;

    private String visibility;

    @JsonProperty("average_speed")
    private double averageSpeed;

    @JsonProperty("max_speed")
    private double maxSpeed;

    @JsonProperty("average_cadence")
    private double averageCadence;

    @JsonProperty("average_watts")
    private double averageWatts;

    @JsonProperty("max_watts")
    private double maxWatts;

    @JsonProperty("weighted_average_watts")
    private double weightedAverageWatts;

    @JsonProperty("device_watts")
    private boolean deviceWatts;

    @JsonProperty("kilojoules")
    private double kiloJoules;

    @JsonProperty("has_heartrate")
    private boolean hasHeartRate;

    @JsonProperty("average_heartrate")
    private double averageHeartRate;

    @JsonProperty("max_heartrate")
    private double maxHeartRate;

    @JsonProperty("display_hide_heartrate_option")
    private boolean displayHideHeartrateOption;

    @JsonProperty("elev_high")
    private double elevationHigh;

    @JsonProperty("elev_low")
    private double elevationLow;

    @JsonProperty("upload_id")
    private long uploadId;

    @JsonProperty("has_kudoed")
    private boolean hasKudoed;
}
