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

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorkerActionExecutor;
import org.cosinus.streamer.ui.action.progress.CopyProgressModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for copying elements based on {@link CopyWorker}
 */
@Component
public class CopyWorkerExecutor<S extends DirectoryStreamer, T extends DirectoryStreamer>
    extends SwingProgressWorkerActionExecutor<CopyActionModel<S, T>, CopyProgressModel> {

    public CopyWorkerExecutor(ProgressFormHandler progressFormHandler,
                              ProgressListenerHandler<CopyProgressModel> progressListenerHandler) {
        super(progressFormHandler,
              progressListenerHandler);
    }

    @Override
    protected ProgressDialog<CopyProgressModel> createProgressDialog(CopyActionModel<S, T> copyModel) {
        return progressFormHandler.createCopyProgressDialog(copyModel);
    }

    @Override
    protected SwingProgressWorker<CopyProgressModel> createSwingWorker(CopyActionModel<S, T> actionModel,
                                                                       ProgressDialog<CopyProgressModel> progressDialog) {
        return new CopyWorker(actionModel, progressDialog);
    }

    @Override
    public String getHandledAction() {
        return CopyActionModel.class.getName();
    }
}
