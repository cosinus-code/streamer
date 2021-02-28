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

import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorkerActionExecutor;
import org.cosinus.streamer.ui.action.progress.ElementsProgressModel;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.context.SwingInjector;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionExecutor} for deleting elements based on {@link DeleteWorker}
 */
@Component
public class DeleteWorkerExecutor
    extends SwingProgressWorkerActionExecutor<DeleteActionModel, ElementsProgressModel> {

    private final SwingInjector swingInjector;

    public DeleteWorkerExecutor(ProgressFormHandler progressFormHandler,
                                ProgressListenerHandler<ElementsProgressModel> progressListenerHandler,
                                SwingInjector swingInjector) {
        super(progressFormHandler,
              progressListenerHandler);
        this.swingInjector = swingInjector;
    }

    @Override
    protected ProgressDialog<ElementsProgressModel> createProgressDialog(DeleteActionModel deleteModel) {
        return progressFormHandler.createElementsProgressDialog(deleteModel);
    }

    @Override
    protected SwingProgressWorker<ElementsProgressModel>
    createSwingWorker(DeleteActionModel actionModel,
                      ProgressDialog<ElementsProgressModel> progressDialog) {
        return swingInjector.inject(DeleteWorker.class,
                                    progressDialog,
                                    actionModel);
    }

    @Override
    public String getHandledAction() {
        return DeleteActionModel.class.getName();
    }
}
