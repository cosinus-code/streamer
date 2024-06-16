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
package org.cosinus.streamer.ui.action.execute.save;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.api.worker.Worker;
import org.cosinus.streamer.ui.action.execute.WorkerExecutor;
import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class SaveWorkerExecutor<T> extends WorkerExecutor<SaveActionModel, SaveWorkerModel<T>, T> {

    public SaveWorkerExecutor(
        final ProgressFormHandler progressFormHandler,
        final WorkerListenerHandler workerListenerHandler) {
        super(progressFormHandler, workerListenerHandler);
    }

    @Override
    public void execute(SaveActionModel actionModel) {
        if (isWorkerRunning(actionModel.getActionId())) {
            return;
        }
        super.execute(actionModel);
    }

    @Override
    protected WorkerListener<SaveWorkerModel<T>, T> createWorkerListener(SaveActionModel actionModel) {
        return actionModel.getStreamerView().getSaveListener();
    }

    @Override
    protected Worker<SaveWorkerModel<T>, T> createSwingWorker(SaveActionModel actionModel) {
        Streamer<T> streamerToSave = actionModel.getStreamerView().getParentStreamer();
        StreamerView<T> streamerView = actionModel.getStreamerView();

        SaveWorkerModel<T> saveWorkerModel = (SaveWorkerModel<T>) streamerToSave.saveModel();
        if (saveWorkerModel == null && !streamerToSave.isParent()) {
            saveWorkerModel = streamerView.getSaveModel();
        }

        return ofNullable(saveWorkerModel)
            .filter(SaveWorkerModel::isDirty)
            .map(workerModel -> new SaveWorker<>(actionModel, workerModel))
            .orElse(null);
    }

    @Override
    public String getHandledAction() {
        return SaveActionModel.class.getName();
    }
}
