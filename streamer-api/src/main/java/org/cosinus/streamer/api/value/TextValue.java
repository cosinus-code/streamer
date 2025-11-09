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

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class TextValue extends Value {

    protected String value;

    public TextValue(Object value) {
        setValue(value);
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    //TODO: to allow empty values
    public void setValue(Object value) {
        this.value = ofNullable(value)
            .map(Object::toString)
            .filter(not(String::isEmpty))
            .orElse(null);
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int compareTo(Value other) {
        if (other instanceof TextValue textValue) {
            return compare(value, textValue.value);
        }
        return compare(this, other);
    }
}
