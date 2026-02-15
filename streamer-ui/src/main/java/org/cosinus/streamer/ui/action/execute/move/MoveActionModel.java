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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.view.ParentStreamerViewContext;

import static org.cosinus.streamer.ui.action.MoveStreamerAction.MOVE_STREAMER_ACTION_ID;

/**
 * Encapsulates the model of the copy streamers action
 */
public class MoveActionModel<S extends Streamer<S>, T extends Streamer<T>> extends CopyActionModel<S, T> {

    public static <S extends Streamer<S>, T extends Streamer<T>>
    MoveActionModel<S, T> move(ParentStreamerViewContext<S> from, ParentStreamerViewContext<T> to) {
        MoveActionModel<S, T> moveActionModel = new MoveActionModel<>();
        moveActionModel.setStreamersToCopy(from.getSelectedItems())
            .from(from.getParentStreamer())
            .to(to.getParentStreamer());

        return moveActionModel;
    }

    @Override
    public String getActionId() {
        return MOVE_STREAMER_ACTION_ID;
    }
}
