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

package org.cosinus.streamer.api.value;

import org.apache.commons.lang3.ObjectUtils;
import org.cosinus.swing.format.FormatHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class DurationValue extends LongValue {

    @Autowired
    private FormatHandler formatHandler;

    protected Long value;

    public DurationValue(Long value) {
        super(value);
    }

    @Override
    public String toString() {
        return ofNullable(value)
            .map(formatHandler::formatTime)
            .orElse("");
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof DurationValue duration) {
            return compare(value, duration.value);
        }
        return compare(this, other);
    }
}
