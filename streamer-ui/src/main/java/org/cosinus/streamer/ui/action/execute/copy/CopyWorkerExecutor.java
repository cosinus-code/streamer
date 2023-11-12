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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.SimpleWorker;
import org.cosinus.streamer.ui.action.execute.WorkerExecutor;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public class CopyWorkerExecutor<S extends Streamer<?>, T extends Streamer<?>>
    extends WorkerExecutor<CopyActionModel<S, T>, CopyProgressModel, CopyProgressModel> {

    public CopyWorkerExecutor(ProgressFormHandler progressFormHandler,
                              WorkerListenerHandler workerListenerHandler) {
        super(progressFormHandler, workerListenerHandler);
    }

    @Override
    protected ProgressDialog<CopyProgressModel> createProgressDialog(CopyActionModel<S, T> copyModel) {
        return progressFormHandler.createCopyProgressDialog(copyModel);
    }

    @Override
    protected SimpleWorker<CopyProgressModel> createSwingWorker(final CopyActionModel<S, T> actionModel) {
        return new CopyWorker<>(actionModel);
    }

    @Override
    public String getHandledAction() {
        return CopyActionModel.class.getName();
    }
}
