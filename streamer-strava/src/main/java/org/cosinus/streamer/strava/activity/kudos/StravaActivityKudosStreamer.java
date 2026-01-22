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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.value.TranslatableName;

import java.util.List;
import java.util.stream.Stream;

import static org.cosinus.streamer.api.value.TranslatableName.translatableNames;

public class StravaActivityKudosStreamer
    extends ExpandedStreamer<StravaActivityKudoStreamer>
    implements ParentStreamer<StravaActivityKudoStreamer> {

    private final List<TranslatableName> detailNames;

    public StravaActivityKudosStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        this.detailNames = translatableNames(DETAIL_KEY_NAME);
    }

    @Override
    public Stream<StravaActivityKudoStreamer> stream() {
        return binaryStreamer().getActivityKudos()
            .stream()
            .map(kudo -> new StravaActivityKudoStreamer(this, kudo));
    }

    @Override
    public StravaActivityKudosJsonStreamer binaryStreamer() {
        return (StravaActivityKudosJsonStreamer) super.binaryStreamer();
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
