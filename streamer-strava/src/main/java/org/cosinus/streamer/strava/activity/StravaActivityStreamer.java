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

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.DateValue;
import org.cosinus.streamer.api.value.DistanceValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.StravaStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;
import org.cosinus.streamer.strava.activity.comments.StravaActivityCommentsJsonStreamer;
import org.cosinus.streamer.strava.activity.gpx.StravaActivityGpxStreamer;
import org.cosinus.streamer.strava.activity.kudos.StravaActivityKudosJsonStreamer;
import org.cosinus.streamer.strava.activity.laps.StravaActivityLapsJsonStreamer;
import org.cosinus.streamer.strava.model.Activity;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StravaActivityStreamer extends StravaParentStreamer<StravaStreamer<?>> {

    public static final String DETAIL_KEY_COUNT = "count";

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
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_COUNT, null)
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
        return activity.getDescription();
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
    public void reset() {
        details = asList(
            new TextValue(getName()),
            new TextValue(getType()),
            new DistanceValue(activity.getDistance()),
            new DistanceValue(activity.getTotalElevationGain()),
            new DateValue(getStartDate()));
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
