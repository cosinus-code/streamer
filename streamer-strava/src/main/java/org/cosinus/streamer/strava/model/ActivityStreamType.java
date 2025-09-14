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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.ArrayList;

import static java.util.Arrays.stream;

public enum ActivityStreamType {
    TIME("time", Integer.class),
    LATITUDE_LONGITUDE("latlng", ArrayList.class),
    ALTITUDE("altitude", Double.class),
    DISTANCE("distance", Double.class),
    HEART_RATE("heartrate", Integer.class),
    CADENCE("cadence", Long.class),
    WATTS("watts", Double.class),
    TEMPERATURE("temp", Double.class),
    VELOCITY_SMOOTH("velocity_smooth", Double.class),
    MOVING("moving", Double.class),
    GRADE_SMOOTH("grade_smooth", Double.class);

    private final String name;

    @Getter
    private final Class<?> dataClass;

    ActivityStreamType(final String name, final Class<?> dataClass) {
        this.name = name;
        this.dataClass = dataClass;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    @JsonCreator
    public static ActivityStreamType fromName(final String name) {
        return stream(values())
            .filter(type -> type.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
