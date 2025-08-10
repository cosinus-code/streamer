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

package org.cosinus.streamer.strava.activity.comments;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.DateValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.model.ActivityComment;

import static java.util.Arrays.asList;

public class StravaActivityCommentStreamer extends StravaJsonStreamer {

    public static final String COMMENT_ICON_NAME = "format-justify-left";

    private final StravaActivityCommentsStreamer stravaActivityCommentsStreamer;

    private final ActivityComment activityComment;

    public StravaActivityCommentStreamer(final StravaActivityCommentsStreamer stravaActivityCommentsStreamer,
                                         final ActivityComment activityComment) {
        super(stravaActivityCommentsStreamer.binaryStreamer().getStravaUserStreamer());
        this.stravaActivityCommentsStreamer = stravaActivityCommentsStreamer;
        this.activityComment = activityComment;
    }

    @Override
    public String getName() {
        return activityComment.getText();
    }

    protected String getAuthorFullName() {
        return activityComment.getAthlete().getFirstname() + " " + activityComment.getAthlete().getLastname();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityCommentsStreamer;
    }

    @Override
    public String getIconName() {
        return COMMENT_ICON_NAME;
    }

    @Override
    public Object getSource() {
        return activityComment;
    }

    @Override
    public void reset() {
        details = asList(
            new TextValue(activityComment.getText()),
            new TextValue(getAuthorFullName()),
            new DateValue(activityComment.getCreatedAt())
        );
    }
}
