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

import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.CopyStreamerAction.COPY_STREAMER_ACTION_ID;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public class CopyWorkerExecutor extends WorkerExecutor<CopyActionModel, CopyWorker<?, ?>> {

    protected final ProgressFormHandler progressFormHandler;

    protected final StreamerViewHandler streamerViewHandler;

    protected CopyWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                 final StreamerViewHandler streamerViewHandler) {
        this.progressFormHandler = progressFormHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected CopyWorker createWorker(CopyActionModel copyModel) {
        CopyWorker copyWorker = new CopyWorker<>(copyModel,
            new CopyWorkerModel<>(copyModel.getDestinationView().getCopyWorkerModel()));
        copyWorker.registerListener(new WorkerListener<WorkerModel<CopyUnit<?, ?>>, CopyUnit<?, ?>>() {
            @Override
            public void workerUpdated(WorkerModel<CopyUnit<?, ?>> workerModel) {
                ofNullable(copyModel.getDestinationView())
                    .ifPresent(StreamerView::fireContentChanged);
            }

            @Override
            public void workerFinished(WorkerModel<CopyUnit<?, ?>> workerModel) {
                ofNullable(copyModel.getSourceView())
                    .ifPresent(StreamerView::reload);
                ofNullable(copyModel.getDestinationView())
                    .ifPresent(StreamerView::reload);
            }
        });
        copyWorker.registerListener(progressFormHandler.createCopyProgressDialog(copyModel, copyWorker.getId()));
        return copyWorker;
    }

    @Override
    public String getHandledAction() {
        return COPY_STREAMER_ACTION_ID;
    }
}
