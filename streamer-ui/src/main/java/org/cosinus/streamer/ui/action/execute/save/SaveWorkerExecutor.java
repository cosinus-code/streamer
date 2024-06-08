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
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
public class SaveWorkerExecutor implements ActionExecutor<SaveActionModel<?>> {

    private final WorkerListenerHandler workerListenerHandler;

    public SaveWorkerExecutor(final WorkerListenerHandler workerListenerHandler) {
        this.workerListenerHandler = workerListenerHandler;
    }

    @Override
    public void execute(SaveActionModel<?> actionModel) {
        executeSave(actionModel);
    }

    private <T> void executeSave(SaveActionModel<T> actionModel) {
        Streamer<T> streamerToSave = actionModel.getStreamerToSave();
        StreamerView<T> streamerView = actionModel.getStreamerView();

        SaveWorkerModel<?> saveWorkerModel = streamerToSave.saveModel();
        if (saveWorkerModel == null && !streamerToSave.isParent()) {
            saveWorkerModel = streamerView.getSaveModel();
        }

        if (saveWorkerModel != null) {
            SaveWorker<?> saveWorker = new SaveWorker<>(actionModel, saveWorkerModel);
            ofNullable(streamerView.getSaveListener())
                .ifPresent(listener -> workerListenerHandler.register(saveWorker.getId(), listener));
            saveWorker.start();
        }
    }

    @Override
    public void cancel(String executionId) {
    }

    @Override
    public void remove(String executionId) {

    }

    @Override
    public String getHandledAction() {
        return SaveActionModel.class.getName();
    }
}
