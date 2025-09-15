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

package org.cosinus.streamer.strava.feed;

import org.cosinus.streamer.strava.StravaParentStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;

import java.util.stream.Stream;

public class StravaFeedStreamer extends StravaParentStreamer<StravaActivityStreamer> {

    public static final String FEED = "Feed";

    public StravaFeedStreamer(StravaUserStreamer stravaUserStreamer) {
        super(stravaUserStreamer, FEED);
    }

    @Override
    public Stream<StravaActivityStreamer> stream() {
        return Stream.empty();
    }
}
