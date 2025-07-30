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
import org.cosinus.streamer.api.expand.ExpandedStreamer;

import java.io.OutputStream;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.streamer.api.ParentStreamer.FOLDER_VIEW_ID;

public class StravaActivityLapsStreamer extends ExpandedStreamer<StravaActivityLapStreamer> {

    public StravaActivityLapsStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    public Stream<StravaActivityLapStreamer> stream() {
        return binaryStreamer().getActivityLapsStream()
            .map(activityLap -> new StravaActivityLapStreamer(this, activityLap));
    }

    @Override
    public StravaActivityLapsBinaryStreamer binaryStreamer() {
        return (StravaActivityLapsBinaryStreamer) super.binaryStreamer();
    }

    @Override
    public String getViewId() {
        return FOLDER_VIEW_ID;
    }

    @Override
    public Optional<StravaActivityLapStreamer> find(String path) {
        return Optional.empty();
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return null;
    }
}
