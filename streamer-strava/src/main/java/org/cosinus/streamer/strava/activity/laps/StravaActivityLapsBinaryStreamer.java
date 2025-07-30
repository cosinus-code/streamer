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
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;
import org.cosinus.streamer.strava.model.ActivityLap;

import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.cosinus.streamer.strava.activity.laps.StravaActivityLapsExpander.STRAVA_ACTIVITY_LAPS;

public class StravaActivityLapsBinaryStreamer extends StravaJsonStreamer {

    public static final String ACTIVITY_LAPS = "Laps";

    public static final String LAPS_ICON_NAME = "undo";

    private final StravaActivityStreamer stravaActivityStreamer;

    private final long stravaActivityId;

    public StravaActivityLapsBinaryStreamer(final StravaActivityStreamer stravaActivityStreamer) {
        super(stravaActivityStreamer.getStravaUserStreamer());
        this.stravaActivityStreamer = stravaActivityStreamer;
        this.stravaActivityId = stravaActivityStreamer.getActivity().getId();
    }

    @Override
    public String getName() {
        return ACTIVITY_LAPS;
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(ACTIVITY_LAPS);
    }

    @Override
    protected Object getSource() {
        return getActivityLapsStream()
            .collect(toList());
    }

    @Override
    public String getType() {
        return STRAVA_ACTIVITY_LAPS;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityStreamer;
    }

    @Override
    public String getIconName() {
        return LAPS_ICON_NAME;
    }

    public Stream<ActivityLap> getActivityLapsStream() {
        return invokeStravaClient(stravaClient -> stravaClient.getActivityLaps(stravaActivityId))
            .stream();
    }

    @Override
    protected Long getCount() {
        return null;
    }
}
