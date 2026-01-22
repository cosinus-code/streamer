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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.value.TranslatableName;

import java.util.List;
import java.util.stream.Stream;

public class StravaActivityCommentsStreamer
    extends ExpandedStreamer<StravaActivityCommentStreamer>
    implements ParentStreamer<StravaActivityCommentStreamer> {

    public static final String DETAIL_KEY_COMMENT = "comment";

    private final List<TranslatableName> detailNames;

    public StravaActivityCommentsStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        this.detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_COMMENT,
            DETAIL_KEY_NAME,
            DETAIL_KEY_TIME
        );
    }

    @Override
    public Stream<StravaActivityCommentStreamer> stream() {
        return binaryStreamer().getActivityComments()
            .stream()
            .map(comment -> new StravaActivityCommentStreamer(this, comment));
    }

    @Override
    public StravaActivityCommentsJsonStreamer binaryStreamer() {
        return (StravaActivityCommentsJsonStreamer) super.binaryStreamer();
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
