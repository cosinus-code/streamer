/*
 * Copyright 2025 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.ui.action.execute.move;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.view.StreamerView;

import java.util.List;

import static org.cosinus.streamer.ui.action.MoveStreamerAction.MOVE_STREAMER_ACTION_ID;

/**
 * Encapsulates the model of the copy streamers action
 */
public class MoveActionModel extends CopyActionModel {

    public static MoveActionModel move() {
        return new MoveActionModel();
    }

    @Override
    public MoveActionModel streamers(List<?> streamersToCopy) {
        super.streamers(streamersToCopy);
        return this;
    }

    @Override
    public MoveActionModel from(StreamerView<?> sourceView) {
        super.from(sourceView);
        return this;
    }

    @Override
    public MoveActionModel from(ParentStreamer<?> source) {
        super.from(source);
        return this;
    }

    @Override
    public MoveActionModel to(ParentStreamer<?> destination, StreamerView<?> destinationView) {
        super.to(destination, destinationView);
        return this;
    }

    @Override
    public MoveActionModel to(StreamerView<?> destinationView) {
        super.to(destinationView);
        return this;
    }

    @Override
    public MoveActionModel to(ParentStreamer<?> destination) {
        super.to(destination);
        return this;
    }

    @Override
    public String getActionId() {
        return MOVE_STREAMER_ACTION_ID;
    }
}
