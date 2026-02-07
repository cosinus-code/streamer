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
import org.cosinus.streamer.api.value.IntegerValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;
import org.cosinus.streamer.strava.model.Activity;
import org.cosinus.streamer.strava.model.AthleteName;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.strava.activity.kudos.StravaActivityKudosExpander.STRAVA_ACTIVITY_KUDOS;

public class StravaActivityKudosJsonStreamer extends StravaJsonStreamer {

    public static final String ACTIVITY_KUDOS = "Kudos";

    public static final String KUDOS_ICON_NAME = "starred";

    public static final int DETAILS_INDEX_COUNT = 1;

    @Autowired
    private Translator translator;

    private final StravaActivityStreamer stravaActivityStreamer;

    private final long stravaActivityId;

    private List<AthleteName> activityKudoers;

    public StravaActivityKudosJsonStreamer(final StravaActivityStreamer stravaActivityStreamer) {
        super(stravaActivityStreamer.getStravaUserStreamer());
        this.stravaActivityStreamer = stravaActivityStreamer;
        this.stravaActivityId = stravaActivityStreamer.getActivity().getId();
    }

    @Override
    public String getName() {
        return ACTIVITY_KUDOS;
    }

    @Override
    public Object getSource() {
        return getActivityKudos();
    }

    @Override
    public String getType() {
        return STRAVA_ACTIVITY_KUDOS;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityStreamer;
    }

    @Override
    public String getIconName() {
        return KUDOS_ICON_NAME;
    }

    public List<AthleteName> getActivityKudos() {
        if (activityKudoers == null) {
            activityKudoers = getKudosCount() > 0 ?
                invokeStravaClient(stravaClient -> stravaClient.getActivityKudos(stravaActivityId)) :
                emptyList();
        }
        return activityKudoers;
    }

    public int getKudosCount() {
        return ofNullable(stravaActivityStreamer.getActivity())
            .map(Activity::getKudosCount)
            .orElse(0);
    }

    @Override
    public void initDetails() {
        details = asList(
            new TextValue(getName()),
            new IntegerValue(getKudosCount())
        );
    }

    @Override
    public void reset() {
        activityKudoers = null;
    }

    @Override
    public String getDescription() {
        return translator.translate("strava-activity-kudos", details().get(DETAILS_INDEX_COUNT));
    }
}
