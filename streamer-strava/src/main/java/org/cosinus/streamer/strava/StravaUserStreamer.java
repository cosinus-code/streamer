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

package org.cosinus.streamer.strava;

import error.StreamerException;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.strava.activity.StravaActivitiesStreamer;
import org.cosinus.streamer.strava.client.StravaClient;
import org.cosinus.streamer.strava.client.StravaClientInvoker;
import org.cosinus.streamer.strava.model.AthleteProfile;
import org.cosinus.streamer.strava.statististics.StravaStatisticsBinaryStreamer;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.strava.StravaMainStreamer.STRAVA_ICON_NAME;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StravaUserStreamer implements ParentStreamer<StravaStreamer> {

    public static final DateTimeFormatter DATE_FORMATTER = ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

    public static final String USER_ID = "id";

    public static final String USERNAME = "username";

    public static final String FIRST_NAME = "firstname";

    public static final String LAST_NAME = "lastname";

    public static final String CITY = "city";

    public static final String STATE = "state";

    public static final String COUNTRY = "country";

    public static final String SEX = "sex";

    public static final String PREMIUM = "premium";

    public static final String SUMMIT = "summit";

    public static final String CREATED_AT = "created_at";

    public static final String UPDATED_AT = "updated_at";

    public static final String WEIGHT = "wight";

    public static final String PROFILE = "profile";

    public static final String PROFILE_MEDIUM = "profile_medium";

    public static final String FRIEND = "friend";

    public static final String FOLLOWER = "follower";

    public static final Set<String> DETAIL_NAMES = Set.of(
        USERNAME,
        FIRST_NAME,
        LAST_NAME
    );

    @Autowired
    private StravaMainStreamer stravaMainStreamer;

    @Autowired
    protected StravaClientInvoker stravaClientInvoker;

    private final String userName;

    private final Map<String, Object> userDetails;

    private final List<Value> details;

    public StravaUserStreamer(final String userName, final Map<String, Object> userDetails) {
        injectContext(this);
        this.userName = userName;
        userDetails.putIfAbsent(USERNAME, userName);
        this.userDetails = userDetails;
        details = List.of(
            new TextValue(userDetails.get(USERNAME)),
            new TextValue(userDetails.get(FIRST_NAME)),
            new TextValue(userDetails.get(LAST_NAME)),
            new TextValue(userDetails.get(CITY)),
            new TextValue(userDetails.get(COUNTRY))
        );
    }

    @Override
    public Stream<StravaStreamer> stream() {
        return Stream.of(
            new StravaActivitiesStreamer(this),
            new StravaStatisticsBinaryStreamer(this)
        );
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public Path getPath() {
        return Paths.get(userName);
    }

    @Override
    public String getIconName() {
        return ofNullable(userDetails.get(PROFILE))
            .map(Object::toString)
            .orElse(STRAVA_ICON_NAME);
    }

    @Override
    public String getProtocol() {
        return stravaMainStreamer.getProtocol();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaMainStreamer;
    }

    @Override
    public List<Value> details() {
        return details;
    }

    @Override
    public boolean canUpdateDetail(int detailIndex) {
        return false;
    }

    public Optional<LocalDateTime> getUserCreatedAt() {
        return ofNullable(userDetails.get(CREATED_AT))
            .map(Object::toString)
            .map(date -> LocalDateTime.parse(date, DATE_FORMATTER))
            .or(() -> ofNullable(getAthleteProfile())
                .map(AthleteProfile::getCreatedAt));
    }

    public Long getUserId() {
        return ofNullable(userDetails.get(USER_ID))
            .map(Object::toString)
            .map(Long::parseLong)
            .or(() -> ofNullable(getAthleteProfile())
                .map(AthleteProfile::getId))
            .orElseThrow(() -> new StreamerException("Failed to solve the current user id"));

    }

    protected AthleteProfile getAthleteProfile() {
        return stravaClientInvoker.invoke(userName, StravaClient::getCurrentAthleteProfile);
    }

}

