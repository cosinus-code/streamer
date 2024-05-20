/*
 * Copyright 2020 Cosinus Software
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.compare;

public class DateValue extends Value {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    protected Date value;

    public DateValue(Long value) {
        this(ofNullable(value)
            .map(Date::new)
            .orElse(null));
    }

    public DateValue(Object value) {
        setValue(value);
    }

    public DateValue(Date date) {
        this.value = date;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public void setValue(Object value) {
        this.value = ofNullable(value)
            .filter(Date.class::isInstance)
            .map(Date.class::cast)
            .or(() -> ofNullable(value)
                .map(Object::toString)
                .filter(not(String::isEmpty))
                .map(this::parseDate))
            .orElse(null);
    }

    @Override
    public Date value() {
        return value;
    }

    private Date parseDate(String text) {
        try {
            return DATE_FORMAT.parse(text);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return ofNullable(value)
            .map(DATE_FORMAT::format)
            .orElse("");
    }

    @Override
    public int compareTo(@NotNull Value other) {
        if (other instanceof DateValue dateValue) {
            return compare(value, dateValue.value);
        }
        return compare(this, other);
    }
}
