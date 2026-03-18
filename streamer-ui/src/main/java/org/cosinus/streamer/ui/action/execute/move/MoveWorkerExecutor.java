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

import org.cosinus.streamer.ui.action.execute.copy.CopyProgressModel;
import org.cosinus.streamer.ui.action.execute.copy.CopyUnit;
import org.cosinus.streamer.ui.action.execute.copy.CopyWorker;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.MoveStreamerAction.MOVE_STREAMER_ACTION_ID;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public class MoveWorkerExecutor
    extends WorkerExecutor<MoveActionModel, WorkerModel<CopyUnit<?, ?>>, CopyUnit<?, ?>, CopyProgressModel<?, ?>> {

    protected final ProgressFormHandler progressFormHandler;

    protected final StreamerViewHandler streamerViewHandler;

    protected MoveWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                 final StreamerViewHandler streamerViewHandler) {
        this.progressFormHandler = progressFormHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected MoveWorker createWorker(MoveActionModel moveModel) {
        MoveWorker moveWorker = new MoveWorker<>(moveModel, new MoveWorkerModel<>(
            moveModel.getDestinationView().getCopyWorkerModel(),
            moveModel.getSourceView().getDeleteWorkerModel()));
        moveWorker.registerListener(new WorkerListener<WorkerModel<CopyUnit<?, ?>>, CopyUnit<?, ?>>() {
            @Override
            public void workerUpdated(WorkerModel<CopyUnit<?, ?>> workerModel) {
                ofNullable(moveModel.getSourceView())
                    .ifPresent(StreamerView::fireContentChanged);
                ofNullable(moveModel.getDestinationView())
                    .ifPresent(StreamerView::fireContentChanged);
            }

            @Override
            public void workerFinished(WorkerModel<CopyUnit<?, ?>> workerModel) {
                ofNullable(moveModel.getSourceView())
                    .ifPresent(StreamerView::reload);
                ofNullable(moveModel.getDestinationView())
                    .ifPresent(StreamerView::reload);
            }
        });
        moveWorker.registerListener(progressFormHandler.createCopyProgressDialog(moveModel, moveWorker.getId()));
        return moveWorker;
    }

    @Override
    public String getHandledAction() {
        return MOVE_STREAMER_ACTION_ID;
    }
}
