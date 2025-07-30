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
import org.cosinus.streamer.api.value.*;
import org.cosinus.streamer.strava.StravaFolderStreamer;
import org.cosinus.streamer.strava.StravaMainStreamer;
import org.cosinus.streamer.strava.StravaStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;
import org.cosinus.streamer.strava.activity.comments.StravaActivityCommentsBinaryStreamer;
import org.cosinus.streamer.strava.activity.laps.StravaActivityLapsBinaryStreamer;
import org.cosinus.streamer.strava.activity.laps.StravaActivityLapsStreamer;
import org.cosinus.streamer.strava.model.Activity;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StravaActivityStreamer extends StravaFolderStreamer<StravaStreamer> {

    public static final String DETAIL_KEY_COUNT = "count";

    private final StravaActivitiesYearStreamer stravaActivitiesYearStreamer;

    @Getter
    private final Activity activity;

    private final List<TranslatableName> detailNames;

    protected List<Value> details;

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
    public Stream<StravaStreamer> stream() {
        return Stream.of(
            new StravaActivityLapsBinaryStreamer(this),
            new StravaActivityCommentsBinaryStreamer(this)
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

    @Override
    public long lastModified() {
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
    public List<Value> details() {
        init();
        return details;
    }

    @Override
    public void init() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new TextValue(getType()),
                new DistanceValue(getSize()),
                new DistanceValue(activity.getTotalElevationGain()),
                new DateValue(lastModified()));
        }
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
