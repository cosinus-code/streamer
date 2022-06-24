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

package org.cosinus.streamer.ui.action.execute;

import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.action.progress.ProgressModel;
import org.cosinus.streamer.ui.dialog.ProgressDialog;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.worker.SwingWorker;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ActionExecutor} for deleting streamers based on {@link ProgressWorker}
 */
public abstract class ProgressWorkerActionExecutor<A extends ActionModel, P extends ProgressModel>
        implements ActionExecutor<A> {

    protected final ProgressFormHandler progressFormHandler;

    protected final ProgressListenerHandler<P> progressListenerHandler;

    private final Map<String, ProgressWorker<P>> workersMap = new ConcurrentHashMap<>();

    protected ProgressWorkerActionExecutor(ProgressFormHandler progressFormHandler,
                                           ProgressListenerHandler<P> progressListenerHandler) {
        this.progressFormHandler = progressFormHandler;
        this.progressListenerHandler = progressListenerHandler;
    }

    @Override
    public void execute(A actionModel) {

        ProgressDialog<P> progressDialog = createProgressDialog(actionModel);
        progressListenerHandler.register(actionModel.getActionId(), progressDialog);

        ProgressWorker<P> worker = createSwingWorker(actionModel, progressDialog);
        workersMap.put(actionModel.getActionId(), worker);

        worker.execute();
        progressListenerHandler.startProgress(actionModel.getActionId());
    }

    @Override
    public void cancel(String copyId) {
        Optional.ofNullable(workersMap.get(copyId))
                .ifPresent(SwingWorker::cancel);
    }

//    @Override
//    public void runInBackground(String copyId) {
//        Optional.ofNullable(workersMap.get(copyId))
//                .ifPresent(worker -> {
//                    //TODO
//                });
//    }

    protected abstract ProgressDialog<P> createProgressDialog(A actionModel);

    protected abstract ProgressWorker<P> createSwingWorker(A actionModel,
                                                           ProgressDialog<P> progressDialog);
}
