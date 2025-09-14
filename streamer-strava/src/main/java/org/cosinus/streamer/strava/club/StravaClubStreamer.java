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

import org.cosinus.streamer.api.KeyValueStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.IntegerValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.model.Club;
import org.cosinus.streamer.strava.model.SportType;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.api.KeyValueStreamer.KEY_VALUE_ICON_NAME;

public class StravaClubStreamer extends StravaParentStreamer<KeyValueStreamer> {

    private final StravaClubsStreamer stravaClubsStreamer;

    private final Club club;

    private final List<TranslatableName> detailNames;

    protected List<Value> details;

    public StravaClubStreamer(final StravaClubsStreamer stravaClubsStreamer,
                              final Club club) {
        super(stravaClubsStreamer.getStravaUserStreamer(), club.getName());
        this.stravaClubsStreamer = stravaClubsStreamer;
        this.club = club;
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_VALUE, null)
        );
        this.details = asList(
            new TextValue(club.getName()),
            new TextValue(club.getCity()),
            new TextValue(club.getCountry()),
            new IntegerValue(club.getMemberCount())
        );
    }

    @Override
    public Stream<KeyValueStreamer> stream() {
        return Stream.of(
            new KeyValueStreamer(this, "club-name", club.getName()),
            new KeyValueStreamer(this, "club-sport-type", getSportType()),
            new KeyValueStreamer(this, "club-city", club.getCity()),
            new KeyValueStreamer(this, "club-state", club.getState()),
            new KeyValueStreamer(this, "club-country", club.getCountry()),
            new KeyValueStreamer(this, "club-private", club.isPrivate()),
            new KeyValueStreamer(this, "club-member-count", club.getMemberCount()),
            new KeyValueStreamer(this, "club-featured", club.isFeatured()),
            new KeyValueStreamer(this, "club-verified", club.isVerified()),
            new KeyValueStreamer(this, "club-url", club.getUrl())
        );
    }

    private String getSportType() {
        return ofNullable(club.getSportType())
            .map(SportType::getName)
            .orElse(null);
    }

    @Override
    public String getType() {
        return "json";
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaClubsStreamer;
    }

    @Override
    public String getIconName() {
        return KEY_VALUE_ICON_NAME;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public List<Value> details() {
        return details;
    }
}
