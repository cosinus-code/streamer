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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.streamer.ui.action.execute.ProgressWorker;
import org.cosinus.streamer.ui.action.execute.ProgressWorkerActionExecutor;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for deleting streamers based on {@link DeleteWorker}
 */
@Component
public class DeleteWorkerExecutor
    extends ProgressWorkerActionExecutor<DeleteActionModel, StreamersProgressModel> {

    public DeleteWorkerExecutor(ProgressFormHandler progressFormHandler,
                                ProgressListenerHandler<StreamersProgressModel> progressListenerHandler) {
        super(progressFormHandler,
              progressListenerHandler);
    }

    @Override
    protected ProgressDialog<StreamersProgressModel> createProgressDialog(DeleteActionModel deleteModel) {
        return progressFormHandler.createStreamersProgressDialog(deleteModel);
    }

    @Override
    protected ProgressWorker<StreamersProgressModel>
    createSwingWorker(DeleteActionModel actionModel,
                      ProgressDialog<StreamersProgressModel> progressDialog) {
        return new DeleteWorker(progressDialog, actionModel);
    }

    @Override
    public String getHandledAction() {
        return DeleteActionModel.class.getName();
    }
}
