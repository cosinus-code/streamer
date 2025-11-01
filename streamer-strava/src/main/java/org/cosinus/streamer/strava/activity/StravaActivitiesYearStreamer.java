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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.cosinus.stream.Streams.pagedStream;
import static org.cosinus.swing.util.DateUtils.*;

public class StravaActivitiesYearStreamer extends StravaParentStreamer<StravaActivityStreamer> {

    public static final String DETAIL_KEY_DISTANCE = "distance";

    public static final String DETAIL_KEY_ELEVATION = "elevation";

    public static final String DETAIL_KEY_START_TIME = "start-time";

    protected final StravaActivitiesStreamer stravaActivitiesStreamer;

    final int year;

    protected final List<TranslatableName> detailNames;

    public StravaActivitiesYearStreamer(final StravaUserStreamer stravaUserStreamer,
                                        final StravaActivitiesStreamer stravaActivitiesStreamer,
                                        final int year) {
        super(stravaUserStreamer, valueOf(year));
        this.stravaActivitiesStreamer = stravaActivitiesStreamer;
        this.year = year;
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_TYPE, null),
            new TranslatableName(DETAIL_KEY_DISTANCE, null),
            new TranslatableName(DETAIL_KEY_ELEVATION, null),
            new TranslatableName(DETAIL_KEY_START_TIME, null));
    }

    @Override
    public Stream<StravaActivityStreamer> stream() {
        long startTime = toEpochSecond(startOfYear(year));
        long endTime = toEpochSecond(year == now().getYear() ? now() : lastSecondOfYear(year));

        return pagedStream((pageSize, page) -> stravaClientInvoker.invoke(userName, stravaClient ->
            stravaClient.getCurrentAthleteActivities(startTime, endTime, pageSize, page)))
            .map(activity -> new StravaActivityStreamer(stravaUserStreamer, this, activity));
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivitiesStreamer;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
