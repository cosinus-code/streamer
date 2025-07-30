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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.expand.BinaryExpander;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.expand.Expander;

import static org.cosinus.streamer.strava.activity.laps.StravaActivityLapsExpander.STRAVA_ACTIVITY_LAPS;

@Expander(STRAVA_ACTIVITY_LAPS)
public class StravaActivityLapsExpander implements BinaryExpander<StravaActivityLapStreamer>  {

    public static final String STRAVA_ACTIVITY_LAPS = "strava-activity-laps";

    @Override
    public ExpandedStreamer<StravaActivityLapStreamer> expand(BinaryStreamer binaryStreamer) {
        return new StravaActivityLapsStreamer(binaryStreamer);
    }
}
