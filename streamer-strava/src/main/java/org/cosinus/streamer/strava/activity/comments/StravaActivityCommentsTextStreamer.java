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
import org.cosinus.streamer.api.value.IntegerValue;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;
import org.cosinus.streamer.strava.model.Activity;
import org.cosinus.streamer.strava.model.ActivityComment;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class StravaActivityCommentsTextStreamer extends StravaStreamer<String> {

    public static final String ACTIVITY_COMMENTS = "Comments";

    public static final String COMMENTS_ICON_NAME = "emblem-documents";

    private final StravaActivityStreamer stravaActivityStreamer;

    private final long stravaActivityId;

    private List<ActivityComment> activityComments;

    public StravaActivityCommentsTextStreamer(final StravaActivityStreamer stravaActivityStreamer) {
        super(stravaActivityStreamer.getStravaUserStreamer());
        this.stravaActivityStreamer = stravaActivityStreamer;
        this.stravaActivityId = stravaActivityStreamer.getActivity().getId();
    }

    @Override
    public Stream<String> stream() {
        return getActivityComments()
            .stream()
            .map(this::buildCommentLine);
    }

    protected String buildCommentLine(ActivityComment comment) {
        return "%s %s: %s".formatted(
            comment.getAthlete().getFirstname(),
            comment.getAthlete().getLastname(),
            comment.getText());
    }

    @Override
    public String getName() {
        return ACTIVITY_COMMENTS;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityStreamer;
    }

    @Override
    public String getIconName() {
        return COMMENTS_ICON_NAME;
    }

    public List<ActivityComment> getActivityComments() {
        if (activityComments == null) {
            activityComments = getCommentsCount() > 0 ?
                invokeStravaClient(stravaClient -> stravaClient.getActivityComments(stravaActivityId)) :
                emptyList();
        }
        return activityComments;
    }

    public int getCommentsCount() {
        return ofNullable(stravaActivityStreamer.getActivity())
            .map(Activity::getCommentCount)
            .orElse(0);
    }

    @Override
    public void reset() {
        details = asList(
            new TextValue(getName()),
            new IntegerValue(getCommentsCount())
        );
        activityComments = null;
    }
}
