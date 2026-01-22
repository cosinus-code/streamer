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
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.value.TranslatableName;

import java.util.List;
import java.util.stream.Stream;

import static org.cosinus.streamer.strava.activity.StravaActivitiesYearStreamer.DETAIL_KEY_ELEVATION;

public class StravaActivityLapsStreamer
    extends ExpandedStreamer<StravaActivityLapStreamer>
    implements ParentStreamer<StravaActivityLapStreamer> {

    public static final String DETAIL_KEY_INDEX = "index";

    public static final String DETAIL_KEY_PACE = "pace";


    private final List<TranslatableName> detailNames;

    public StravaActivityLapsStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        this.detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_INDEX,
            DETAIL_KEY_PACE,
            DETAIL_KEY_ELEVATION
        );
    }

    @Override
    public Stream<StravaActivityLapStreamer> stream() {
        return binaryStreamer().getActivityLaps()
            .stream()
            .map(activityLap -> new StravaActivityLapStreamer(this, activityLap));
    }

    @Override
    public StravaActivityLapsJsonStreamer binaryStreamer() {
        return (StravaActivityLapsJsonStreamer) super.binaryStreamer();
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    public int getLapsCount() {
        return binaryStreamer().getActivityLaps().size();
    }
}
