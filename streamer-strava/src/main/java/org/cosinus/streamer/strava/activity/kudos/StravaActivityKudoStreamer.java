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

package org.cosinus.streamer.strava.activity.kudos;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.model.AthleteName;

import static java.util.Collections.singletonList;

public class StravaActivityKudoStreamer extends StravaJsonStreamer {

    public static final String USER_ICON_NAME = "user_icon";

    private final StravaActivityKudosStreamer stravaActivityKudosStreamer;

    private final AthleteName activityKudoer;

    public StravaActivityKudoStreamer(final StravaActivityKudosStreamer stravaActivityKudosStreamer,
                                      final AthleteName activityKudoer) {
        super(stravaActivityKudosStreamer.binaryStreamer().getStravaUserStreamer());
        this.stravaActivityKudosStreamer = stravaActivityKudosStreamer;
        this.activityKudoer = activityKudoer;
    }

    @Override
    public String getName() {
        return activityKudoer.getFirstname() + " " + activityKudoer.getLastname();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityKudosStreamer;
    }

    @Override
    public String getIconName() {
        return USER_ICON_NAME;
    }

    @Override
    public Object getSource() {
        return activityKudoer;
    }

    @Override
    public void reset() {
        details = singletonList(new TextValue(getName()));
    }
}
