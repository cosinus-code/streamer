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

import org.jetbrains.annotations.NotNull;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class IntegerValue extends Value {

    protected Integer value;

    public IntegerValue(Integer value) {
        this.value = value;
    }

    public IntegerValue(Object value) {
        setValue(value);
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public void setValue(Object value) {
        this.value = ofNullable(value)
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .or(() -> ofNullable(value)
                .map(Object::toString)
                .filter(not(String::isEmpty))
                .map(Integer::valueOf))
            .orElse(null);
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof IntegerValue integerValue) {
            return Integer.compare(value, integerValue.value);
        }
        return compare(this, other);
    }
}
