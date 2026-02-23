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
import org.cosinus.streamer.ui.action.execute.copy.AbstractCopyWorkerExecutor;
import org.cosinus.streamer.ui.action.execute.copy.CopyWorker;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.MoveStreamerAction.MOVE_STREAMER_ACTION_ID;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public class MoveWorkerExecutor extends AbstractCopyWorkerExecutor<MoveActionModel> {

    protected MoveWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                 final StreamerViewHandler streamerViewHandler) {
        super(progressFormHandler, streamerViewHandler);
    }

    @Override
    protected MoveWorker internalCreateWorker(MoveActionModel actionModel) {
        return new MoveWorker<>(actionModel, new MoveWorkerModel<>(
            streamerViewHandler.getOppositeView().getCopyWorkerModel(),
            streamerViewHandler.getCurrentView().getDeleteWorkerModel()));
    }

    @Override
    public String getHandledAction() {
        return MOVE_STREAMER_ACTION_ID;
    }
}
