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
package org.cosinus.streamer.ui.action.execute.save;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerExecutor;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.SaveAction.SAVE_ACTION_ID;

@Component
public class SaveWorkerExecutor<T> extends WorkerExecutor<SaveActionModel<T>, SaveWorkerModel<T>, T, ProgressModel> {

    @Override
    protected boolean isValid(Worker<SaveWorkerModel<T>, T, ProgressModel> workerModel) {
        return !isWorkerRunning(workerModel.getId());
    }

    @Override
    protected Worker<SaveWorkerModel<T>, T, ProgressModel> createWorker(SaveActionModel<T> actionModel) {
        Streamer<T> streamerToSave = actionModel.getStreamerView().getParentStreamer();
        StreamerView<T> streamerView = actionModel.getStreamerView();

        SaveWorkerModel<T> saveWorkerModel = ofNullable(streamerToSave)
            .filter(Streamer::isParent)
            .map(streamer -> (SaveWorkerModel<T>) streamer.saveModel())
            .orElseGet(streamerView::getSaveWorkerModel);

        return ofNullable(saveWorkerModel)
            .filter(SaveWorkerModel::isDirty)
            .map(workerModel -> createSaveWorker(actionModel, workerModel))
            .orElse(null);
    }

    private Worker<SaveWorkerModel<T>, T, ProgressModel> createSaveWorker(final SaveActionModel<T> actionModel,
                                                                          final SaveWorkerModel<T> workerModel) {
        return new SaveWorker<>(actionModel, workerModel)
            .registerListener(actionModel.getStreamerView().getSaveListener())
            .registerListener(actionModel.getStreamerView().getLoadingIndicator());
    }

    @Override
    public String getHandledAction() {
        return SAVE_ACTION_ID;
    }
}
