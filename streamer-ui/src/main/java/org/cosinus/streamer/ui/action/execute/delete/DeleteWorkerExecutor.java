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

import org.cosinus.streamer.api.worker.SimpleWorker;
import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.api.worker.WorkerExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.DeleteStreamerAction.DELETE_STREAMER_ACTION_NAME;

/**
 * Implementation of {@link ActionExecutor} for deleting streamers based on {@link DeleteWorker}
 */
@Component
public class DeleteWorkerExecutor
    extends WorkerExecutor<DeleteActionModel, StreamersProgressModel, StreamersProgressModel> {

    protected final ProgressFormHandler progressFormHandler;

    private final LoadActionExecutor loadActionExecutor;

    private final StreamerViewHandler streamerViewHandler;

    protected DeleteWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                   final WorkerListenerHandler workerListenerHandler,
                                   final LoadActionExecutor loadActionExecutor,
                                   final StreamerViewHandler streamerViewHandler) {
        super(workerListenerHandler);
        this.progressFormHandler = progressFormHandler;
        this.loadActionExecutor = loadActionExecutor;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected void registerWorkerListeners(DeleteActionModel deleteAction, StreamersProgressModel workerModel) {
        super.registerWorkerListeners(deleteAction, workerModel);

        workerListenerHandler.register(StreamersProgressModel.class, deleteAction.getExecutionId(),
            new WorkerListener<>() {
                @Override
                public void workerFinished(StreamersProgressModel workerModel) {
                    final StreamerView<?, ?> currentView = streamerViewHandler.getCurrentView();
                    loadActionExecutor.execute(new LoadActionModel(
                        currentView.getCurrentLocation(),
                        currentView.getParentStreamer(),
                        currentView.getNextItemIdentifier()));
                }
            });

    }

    @Override
    protected ProgressDialog<StreamersProgressModel> createWorkerListener(DeleteActionModel deleteModel) {
        return progressFormHandler.createStreamersProgressDialog(deleteModel);
    }

    @Override
    protected SimpleWorker<StreamersProgressModel> createWorker(DeleteActionModel actionModel) {
        return new DeleteWorker(actionModel);
    }

    @Override
    public String getHandledAction() {
        return DELETE_STREAMER_ACTION_NAME;
    }
}
