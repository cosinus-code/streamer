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

package org.cosinus.streamer.strava.activity.laps;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.strava.model.ActivityLap;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.cosinus.streamer.strava.activity.laps.StravaActivityLapsBinaryStreamer.LAPS_ICON_NAME;

public class StravaActivityLapStreamer implements Streamer<ActivityLapStreamable> {

    private final StravaActivityLapsStreamer stravaActivityLapsStreamer;

    private final ActivityLap activityLap;

    public StravaActivityLapStreamer(final StravaActivityLapsStreamer stravaActivityLapsStreamer,
                                     final ActivityLap activityLap) {
        this.stravaActivityLapsStreamer = stravaActivityLapsStreamer;
        this.activityLap = activityLap;
    }

    @Override
    public Stream<ActivityLapStreamable> stream() {
        return Stream.empty();
    }

    @Override
    public String getName() {
        return activityLap.getName();
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(getName());
    }

    @Override
    public String getIconName() {
        return LAPS_ICON_NAME;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityLapsStreamer.getParent();
    }
}
