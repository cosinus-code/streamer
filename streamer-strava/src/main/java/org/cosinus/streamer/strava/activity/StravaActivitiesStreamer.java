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

package org.cosinus.streamer.strava.activity;

import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaFolderStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.stream.IntStream.rangeClosed;

public class StravaActivitiesStreamer extends StravaFolderStreamer<StravaActivitiesYearStreamer> {

    public static final String ACTIVITIES = "Activities";

    public StravaActivitiesStreamer(final StravaUserStreamer stravaUserStreamer) {
        super(stravaUserStreamer, ACTIVITIES);
    }

    @Override
    public Stream<StravaActivitiesYearStreamer> stream() {
        int currentYear = now().getYear();
        int startYear = stravaUserStreamer.getUserCreatedAt()
            .map(LocalDateTime::getYear)
            .orElse(currentYear);
        return rangeClosed(startYear, currentYear)
            .mapToObj(year -> new StravaActivitiesYearStreamer(stravaUserStreamer, this, year));
    }

    @Override
    public void reset() {
        details = singletonList(new TextValue(getName()));
    }
}
