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

package org.cosinus.streamer.strava.club;

import org.cosinus.streamer.api.page.PagedStream;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class StravaClubsStreamer extends StravaParentStreamer<StravaClubStreamer> {

    public static final String CLUBS = "Clubs";

    public static final String DETAIL_KEY_CITY = "club-city";

    public static final String DETAIL_KEY_COUNTRY = "club-country";

    public static final String DETAIL_KEY_MEMBERS_COUNT = "club-member-count";

    private final List<TranslatableName> detailNames;

    public StravaClubsStreamer(final StravaUserStreamer stravaUserStreamer) {
        super(stravaUserStreamer, CLUBS);
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_CITY, null),
            new TranslatableName(DETAIL_KEY_COUNTRY, null),
            new TranslatableName(DETAIL_KEY_MEMBERS_COUNT, null)
        );
    }

    @Override
    public Stream<StravaClubStreamer> stream() {
        return PagedStream
            .of((pageSize, page) -> stravaClientInvoker.invoke(userName, stravaClient ->
                stravaClient.getCurrentAthleteClubs(pageSize, page)))
            .map(club -> new StravaClubStreamer(this, club));
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
