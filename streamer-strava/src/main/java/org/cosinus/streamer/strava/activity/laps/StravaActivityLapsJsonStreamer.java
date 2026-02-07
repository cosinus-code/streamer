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
import org.cosinus.streamer.api.value.IntegerValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;
import org.cosinus.streamer.strava.model.ActivityLap;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Arrays.asList;
import static org.cosinus.streamer.strava.activity.laps.StravaActivityLapsExpander.STRAVA_ACTIVITY_LAPS;

public class StravaActivityLapsJsonStreamer extends StravaJsonStreamer {

    public static final String ACTIVITY_LAPS = "Laps";

    public static final String LAPS_ICON_NAME = "undo";

    @Autowired
    private Translator translator;

    private final StravaActivityStreamer stravaActivityStreamer;

    private final long stravaActivityId;

    private List<ActivityLap> activityLaps;

    public StravaActivityLapsJsonStreamer(final StravaActivityStreamer stravaActivityStreamer) {
        super(stravaActivityStreamer.getStravaUserStreamer());
        this.stravaActivityStreamer = stravaActivityStreamer;
        this.stravaActivityId = stravaActivityStreamer.getActivity().getId();
    }

    @Override
    public String getName() {
        return ACTIVITY_LAPS;
    }

    @Override
    public Object getSource() {
        return getActivityLaps();
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

    public List<ActivityLap> getActivityLaps() {
        if (activityLaps == null) {
            activityLaps = invokeStravaClient(stravaClient ->
                stravaClient.getActivityLaps(stravaActivityId));
        }
        return activityLaps;
    }

    @Override
    protected void initDetails() {
        details = asList(
            new TextValue(getName()),
            new IntegerValue(null)
        );
    }

    @Override
    public void reset() {
        activityLaps = null;
    }

    @Override
    public String getDescription() {
        return translator.translate("strava-activity-laps");
    }
}
