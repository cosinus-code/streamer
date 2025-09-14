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

package org.cosinus.streamer.strava.profile;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cosinus.streamer.api.KeyValueStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.strava.StravaUserStreamer.*;

public class StravaProfileStreamer extends StravaParentStreamer<KeyValueStreamer> {

    public static final String PROFILE = "Profile";

    private final List<TranslatableName> detailNames;

    public StravaProfileStreamer(final StravaUserStreamer stravaUserStreamer) {
        super(stravaUserStreamer, PROFILE);
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_VALUE, null)
        );
    }

    @Override
    public Stream<KeyValueStreamer> stream() {
        return stravaUserStreamer.getUserDetails()
            .entrySet()
            .stream()
            .filter(entry -> !IGNORED_DETAILS.contains(entry.getKey()))
            .map(entry ->
                DATE_DETAILS.contains(entry.getKey()) ?
                    toDate(entry.getValue())
                        .<Entry<String, Object>>map(date -> new ImmutablePair<>(entry.getKey(), date))
                        .orElse(entry) :
                    entry)
            .map(entry -> new KeyValueStreamer(this, entry));
    }

    private Optional<Date> toDate(Object value) {
        return ofNullable(value)
            .map(Object::toString)
            .map(date -> LocalDateTime.parse(date, DATE_FORMATTER))
            .map(localDateTime -> localDateTime.atZone(ZoneId.systemDefault()))
            .map(ZonedDateTime::toInstant)
            .map(Date::from);
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
