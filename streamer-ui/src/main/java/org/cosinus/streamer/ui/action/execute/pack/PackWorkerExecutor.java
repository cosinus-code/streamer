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

package org.cosinus.streamer.ui.action.execute.pack;

import org.cosinus.streamer.ui.action.execute.copy.CopyProgressModel;
import org.cosinus.streamer.ui.action.execute.copy.CopyUnit;
import org.cosinus.streamer.ui.action.execute.copy.CopyWorkerModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.PackStreamerAction.PACK_STREAMER_ACTION_ID;

@Component
public class PackWorkerExecutor
    extends WorkerExecutor<PackActionModel, WorkerModel<CopyUnit<?, ?>>, CopyUnit<?, ?>, CopyProgressModel<?, ?>> {

    protected final ProgressFormHandler progressFormHandler;

    protected final StreamerViewHandler streamerViewHandler;

    protected PackWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                 final StreamerViewHandler streamerViewHandler) {
        this.progressFormHandler = progressFormHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected PackWorker createWorker(PackActionModel packModel) {
        PackWorker packWorker = new PackWorker<>(packModel,
            new CopyWorkerModel<>(packModel.getDestinationView().getCopyWorkerModel()));
        packWorker.registerListener(new WorkerListener<WorkerModel<CopyUnit<?, ?>>, CopyUnit<?, ?>>() {
            @Override
            public void workerUpdated(WorkerModel<CopyUnit<?, ?>> workerModel) {
                ofNullable(packModel.getDestinationView())
                    .ifPresent(StreamerView::fireContentChanged);
            }

            @Override
            public void workerFinished(WorkerModel<CopyUnit<?, ?>> workerModel) {
                ofNullable(packModel.getSourceView())
                    .ifPresent(StreamerView::reload);
                ofNullable(packModel.getDestinationView())
                    .ifPresent(StreamerView::reload);
            }
        });
        packWorker.registerListener(progressFormHandler.createCopyProgressDialog(packModel, packWorker.getId()));
        return packWorker;
    }

    @Override
    public String getHandledAction() {
        return PACK_STREAMER_ACTION_ID;
    }
}
