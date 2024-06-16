/*
 * Copyright 2020 Cosinus Software
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
import org.cosinus.streamer.api.worker.SimpleWorker;
import org.cosinus.streamer.ui.action.execute.WorkerExecutor;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.copy.CopyProgressModel;
import org.cosinus.streamer.ui.action.execute.copy.CopyWorker;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public class MoveWorkerExecutor<S extends Streamer<S>, T extends Streamer<T>>
    extends WorkerExecutor<MoveActionModel<S, T>, CopyProgressModel, CopyProgressModel> {

    protected MoveWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                 final WorkerListenerHandler workerListenerHandler) {
        super(progressFormHandler, workerListenerHandler);
    }

    @Override
    protected ProgressDialog<CopyProgressModel> createWorkerListener(MoveActionModel<S, T> copyModel) {
        return progressFormHandler.createCopyProgressDialog(copyModel);
    }

    @Override
    protected SimpleWorker<CopyProgressModel> createSwingWorker(final MoveActionModel<S, T> actionModel) {
        return new MoveWorker<>(actionModel);
    }

    @Override
    public String getHandledAction() {
        return MoveActionModel.class.getName();
    }
}
