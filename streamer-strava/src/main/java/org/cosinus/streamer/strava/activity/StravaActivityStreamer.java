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

import com.google.maps.internal.PolylineEncoding;
import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.*;
import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.StravaStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;
import org.cosinus.streamer.strava.activity.comments.StravaActivityCommentsJsonStreamer;
import org.cosinus.streamer.strava.activity.gpx.StravaActivityGpxStreamer;
import org.cosinus.streamer.strava.activity.kudos.StravaActivityKudosJsonStreamer;
import org.cosinus.streamer.strava.activity.laps.StravaActivityLapsJsonStreamer;
import org.cosinus.streamer.strava.model.Activity;
import org.cosinus.streamer.strava.model.Map;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static org.cosinus.swing.color.Colors.getColorDescription;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.image.icon.IconHandler.SHAPE_ICON;
import static org.cosinus.swing.image.icon.IconHandler.SHAPE_SEPARATOR;

public class StravaActivityStreamer extends StravaParentStreamer<StravaStreamer<?>> {

    public static final String DETAIL_KEY_COUNT = "count";

    public static final int DETAILS_INDEX_TYPE = 1;

    public static final int DETAILS_INDEX_DISTANCE = 2;

    public static final int DETAILS_INDEX_ELEVATION = 3;

    public static final Color STRAVA_COLOR = new Color(252, 76, 2);

    public static final Color BACKGROUND_COLOR = new Color(175, 229, 176);

    @Autowired
    private Translator translator;

    private final StravaActivitiesYearStreamer stravaActivitiesYearStreamer;

    @Getter
    private final Activity activity;

    private final List<TranslatableName> detailNames;

    public StravaActivityStreamer(final StravaUserStreamer stravaUserStreamer,
                                  final StravaActivitiesYearStreamer stravaActivitiesYearStreamer,
                                  final Activity activity) {
        super(stravaUserStreamer, activity.getName());
        injectContext(this);
        this.stravaActivitiesYearStreamer = stravaActivitiesYearStreamer;
        this.activity = activity;
        this.detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_NAME,
            DETAIL_KEY_COUNT
        );
    }

    @Override
    public Stream<StravaStreamer<?>> stream() {
        return Stream.of(
            new StravaActivityLapsJsonStreamer(this),
            new StravaActivityCommentsJsonStreamer(this),
            new StravaActivityKudosJsonStreamer(this),
            new StravaActivityGpxStreamer(this)
        );
    }

    @Override
    public String getId() {
        return ofNullable(activity.getId())
            .map(Object::toString)
            .orElseGet(this::getName);
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(getId());
    }

    @Override
    public String getName() {
        return activity.getName();
    }

    @Override
    public String getDescription() {
        return Stream.of(
                activity.getDescription(),
                details().get(DETAILS_INDEX_TYPE),
                ofNullable(details().get(DETAILS_INDEX_DISTANCE))
                    .map(distance -> translator.translate("strava-activity-distance", distance))
                    .orElse(null),
                ofNullable(details().get(DETAILS_INDEX_ELEVATION))
                    .map(elevation -> translator.translate("strava-activity-elevation", elevation))
                    .orElse(null))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(not(String::isBlank))
            .collect(joining(", "));
    }

    public long getStartDate() {
        return activity.getStartDate().getTime();
    }

    @Override
    public String getType() {
        return activity.getSportType().getName();
    }

    @Override
    public long getSize() {
        return activity.getDistance().longValue();
    }

    @Override
    public String getProtocol() {
        return getParent().getProtocol();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivitiesYearStreamer;
    }

    @Override
    public void initDetails() {
        details = asList(
            new TextValue(getName()),
            new TextValue(getType()),
            new DistanceValue(activity.getDistance()),
            new DistanceValue(activity.getTotalElevationGain()),
            new PaceValue(activity.getAverageSpeed()),
            new DateValue(getStartDate()));
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public String getIconName() {
        return ofNullable(activity.getMap())
            .map(Map::getSummaryPolyline)
            .map(this::buildShapeIconName)
            .orElse(null);
    }

    protected String buildShapeIconName(String polyline) {
        return concat(
            Stream.of(SHAPE_ICON,
                getColorDescription(BACKGROUND_COLOR),
                getColorDescription(STRAVA_COLOR)),
            ofNullable(polyline)
                .map(PolylineEncoding::decode)
                .stream()
                .flatMap(Collection::stream)
                .map(latlng -> Stream.of(latlng.lat, latlng.lng)
                    .map(Object::toString)
                    .collect(joining(","))))
            .collect(joining(SHAPE_SEPARATOR));
    }

    @Override
    public boolean isIconRounded() {
        return true;
    }
}
