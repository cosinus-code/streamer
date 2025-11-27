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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerListenerHandler;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public abstract class AbstractCopyWorkerExecutor<
    S extends Streamer<S>,
    T extends Streamer<T>,
    A extends CopyActionModel<S, T>> extends WorkerExecutor<A, CopyProgressModel, CopyProgressModel> {

    protected final ProgressFormHandler progressFormHandler;

    private final LoadActionExecutor loadActionExecutor;

    private final StreamerViewHandler streamerViewHandler;

    protected AbstractCopyWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                         final WorkerListenerHandler workerListenerHandler,
                                         final LoadActionExecutor loadActionExecutor,
                                         final StreamerViewHandler streamerViewHandler) {
        super(workerListenerHandler);
        this.progressFormHandler = progressFormHandler;
        this.loadActionExecutor = loadActionExecutor;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected void registerWorkerListeners(A copyAction, CopyProgressModel workerModel) {
        super.registerWorkerListeners(copyAction, workerModel);
        workerListenerHandler.register(CopyProgressModel.class, copyAction.getExecutionId(),
            new WorkerListener<>() {
                @Override
                public void workerFinished(CopyProgressModel workerModel) {
                    final StreamerView<?, ?> oppositeView = streamerViewHandler.getOppositeView();
                    loadActionExecutor.execute(new LoadActionModel(
                        oppositeView.getCurrentLocation(),
                        oppositeView.getParentStreamer(),
                        null));

                    final StreamerView<?, ?> currentView = streamerViewHandler.getCurrentView();
                    loadActionExecutor.execute(new LoadActionModel(
                        currentView.getCurrentLocation(),
                        currentView.getParentStreamer(),
                        currentView.getNextItemIdentifier()));
                }
            });
    }

    @Override
    protected ProgressDialog<CopyProgressModel> createWorkerListener(A copyModel) {
        return progressFormHandler.createCopyProgressDialog(copyModel);
    }
}
