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
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public abstract class AbstractCopyWorkerExecutor<S extends Streamer<S>,
    T extends Streamer<T>,
    A extends CopyActionModel<S, T>>
    extends WorkerExecutor<A, WorkerModel<CopyWorkerUnit<S, T>>, CopyWorkerUnit<S, T>, CopyProgressModel<S>> {

    protected final ProgressFormHandler progressFormHandler;

    protected final StreamerViewHandler streamerViewHandler;

    protected AbstractCopyWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                         final StreamerViewHandler streamerViewHandler) {
        this.progressFormHandler = progressFormHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected WorkerListener<WorkerModel<CopyWorkerUnit<S, T>>, CopyWorkerUnit<S, T>> getWorkerListener(A actionModel) {
        return new WorkerListener<>() {
            @Override
            public void workerUpdated(WorkerModel<CopyWorkerUnit<S, T>> workerModel) {
                streamerViewHandler.getCurrentView().fireContentChanged();
                streamerViewHandler.getOppositeView().fireContentChanged();
            }

            @Override
            public void workerFinished(WorkerModel<CopyWorkerUnit<S, T>> workerModel) {
                streamerViewHandler.reloadViews();
            }
        };
    }

    @Override
    protected ProgressDialog<CopyProgressModel<S>> getProgressListener(A copyModel) {
        return progressFormHandler.createCopyProgressDialog(copyModel);
    }
}
