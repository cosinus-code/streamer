/*
 * Copyright 2025 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.api.value;

import org.cosinus.swing.format.FormatHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class DistanceValue extends LongValue {

    private final boolean computing;

    @Autowired
    private FormatHandler formatHandler;

    public DistanceValue(Double value) {
        this(ofNullable(value)
            .map(Double::longValue)
            .orElse(null));
    }

    public DistanceValue(Long value) {
        this(value, false);
    }

    public DistanceValue(Long value, boolean computing) {
        super(value);
        this.computing = computing;
    }

    @Override
    public String toString() {
        String distance = ofNullable(value)
            .map(formatHandler::formatDistance)
            .orElse("");
        return computing ? "...".concat(distance) : distance;
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof DistanceValue distance) {
            return compare(value, distance.value);
        }
        return compare(this, other);
    }
}
