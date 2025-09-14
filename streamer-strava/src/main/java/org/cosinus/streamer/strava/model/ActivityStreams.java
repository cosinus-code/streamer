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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Optional.ofNullable;

@JsonInclude(NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityStreams extends LinkedHashMap<ActivityStreamType, ActivityStream<?>> {

    @JsonIgnore
    public <T> List<T> getDataStream(ActivityStreamType type) {
        return (List<T>) getDataStream(type, type.getDataClass());
    }

    private <T> List<T> getDataStream(ActivityStreamType type, Class<T> dataClass) {
        return ofNullable(get(type))
            .map(ActivityStream::getData)
            .stream()
            .flatMap(Collection::stream)
            .filter(dataClass::isInstance)
            .map(dataClass::cast)
            .toList();
    }
}
