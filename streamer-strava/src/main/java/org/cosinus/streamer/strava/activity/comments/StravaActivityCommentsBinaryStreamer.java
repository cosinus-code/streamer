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
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.activity.StravaActivityStreamer;
import org.cosinus.streamer.strava.model.ActivityComment;

import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.cosinus.streamer.strava.activity.comments.StravaActivityCommentsExpander.STRAVA_ACTIVITY_COMMENTS;

public class StravaActivityCommentsBinaryStreamer extends StravaJsonStreamer {

    public static final String ACTIVITY_COMMENTS = "Comments";

    public static final String COMMENTS_ICON_NAME = "emblem-documents";

    private final StravaActivityStreamer stravaActivityStreamer;

    private final long stravaActivityId;

    public StravaActivityCommentsBinaryStreamer(final StravaActivityStreamer stravaActivityStreamer) {
        super(stravaActivityStreamer.getStravaUserStreamer());
        this.stravaActivityStreamer = stravaActivityStreamer;
        this.stravaActivityId = stravaActivityStreamer.getActivity().getId();
    }

    @Override
    public String getName() {
        return ACTIVITY_COMMENTS;
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(ACTIVITY_COMMENTS);
    }

    @Override
    protected Object getSource() {
        return getActivityCommentsStream()
            .collect(toList());
    }

    @Override
    public String getType() {
        return STRAVA_ACTIVITY_COMMENTS;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaActivityStreamer;
    }

    @Override
    public String getIconName() {
        return COMMENTS_ICON_NAME;
    }

    public Stream<ActivityComment> getActivityCommentsStream() {
        return invokeStravaClient(stravaClient -> stravaClient.getActivityComments(stravaActivityId))
            .stream();
    }

    @Override
    protected Long getCount() {
        return stravaActivityStreamer.getActivity().getCommentCount();
    }
}
