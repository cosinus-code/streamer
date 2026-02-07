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
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static org.cosinus.stream.Streams.pagedStream;
import static org.cosinus.swing.util.DateUtils.*;

public class StravaActivitiesYearStreamer extends StravaParentStreamer<StravaActivityStreamer> {

    public static final String DETAIL_KEY_DISTANCE = "distance";

    public static final String DETAIL_KEY_ELEVATION = "elevation";

    public static final String DETAIL_KEY_START_TIME = "start-time";

    public static final String DETAIL_KEY_PACE = "pace";

    @Autowired
    private Translator translator;

    protected final StravaActivitiesStreamer stravaActivitiesStreamer;

    final int year;

    protected final List<TranslatableName> detailNames;

    public StravaActivitiesYearStreamer(final StravaUserStreamer stravaUserStreamer,
                                        final StravaActivitiesStreamer stravaActivitiesStreamer,
                                        final int year) {
        super(stravaUserStreamer, valueOf(year));
        this.stravaActivitiesStreamer = stravaActivitiesStreamer;
        this.year = year;
        this.detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_NAME,
            DETAIL_KEY_TYPE,
            DETAIL_KEY_DISTANCE,
            DETAIL_KEY_ELEVATION,
            DETAIL_KEY_PACE,
            DETAIL_KEY_START_TIME
        );
    }

    @Override
    public Stream<StravaActivityStreamer> stream() {
        long startTime = toEpochSecond(startOfYear(year));
        long endTime = toEpochSecond(lastSecondOfYear(year));

        return pagedStream((pageSize, page) -> stravaClientInvoker
            .invoke(userName, stravaClient -> stravaClient
                .getCurrentAthleteActivities(startTime, endTime, pageSize, page)))
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

    @Override
    public String getDescription() {
        return translator.translate("strava-athlete-year-activities", getName());
    }

    @Override
    public void reset() {
        stravaClientInvoker.reset(userName);
    }
}
