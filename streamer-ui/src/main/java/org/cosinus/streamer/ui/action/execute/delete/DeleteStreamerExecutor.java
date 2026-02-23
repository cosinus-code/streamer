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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.progress.ProgressListener;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.DeleteStreamerAction.DELETE_STREAMER_ACTION_ID;

/**
 * Implementation of {@link ActionExecutor} for deleting streamers based on {@link DeleteWorker}
 */
@Component
public class DeleteStreamerExecutor
    extends WorkerExecutor<DeleteActionModel, WorkerModel<Streamer<?>>, Streamer<?>, StreamersProgressModel> {

    protected final ProgressFormHandler progressFormHandler;

    private final StreamerViewHandler streamerViewHandler;

    protected DeleteStreamerExecutor(final ProgressFormHandler progressFormHandler,
                                     final StreamerViewHandler streamerViewHandler) {
        this.progressFormHandler = progressFormHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected WorkerListener<WorkerModel<Streamer<?>>, Streamer<?>> getWorkerListener(DeleteActionModel deleteModel) {
        return new WorkerListener<>() {
            @Override
            public void workerUpdated(WorkerModel<Streamer<?>> workerModel) {
                streamerViewHandler.getCurrentView().fireContentChanged();
            }

            @Override
            public void workerFinished(WorkerModel<Streamer<?>> workerModel) {
                streamerViewHandler.getCurrentView().reload();
            }
        };
    }

    @Override
    protected ProgressListener<StreamersProgressModel> getProgressListener(
        DeleteActionModel deleteModel, Worker<WorkerModel<Streamer<?>>, Streamer<?>, StreamersProgressModel> worker) {

        return progressFormHandler.createStreamersProgressDialog(deleteModel, worker.getId());
    }

    @Override
    protected DeleteWorker createWorker(DeleteActionModel deleteModel) {
        return new DeleteWorker(deleteModel, streamerViewHandler.getCurrentView().getDeleteWorkerModel());
    }

    @Override
    public String getHandledAction() {
        return DELETE_STREAMER_ACTION_ID;
    }
}
