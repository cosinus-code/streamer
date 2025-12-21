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

package org.cosinus.streamer.ui.action.execute.load.image;

import org.cosinus.swing.progress.ProgressListener;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.image.UpdatableImage;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.execute.load.image.LoadImageActionModel.LOAD_IMAGE_ACTION_ID;

@Component
public class LoadImageExecutor
    extends WorkerExecutor<LoadImageActionModel, LoadWorkerModel<UpdatableImage>, UpdatableImage, ProgressModel> {

    @Override
    public String getHandledAction() {
        return LOAD_IMAGE_ACTION_ID;
    }

    @Override
    protected WorkerListener<LoadWorkerModel<UpdatableImage>, UpdatableImage>
    getWorkerListener(LoadImageActionModel actionModel) {
        return actionModel.getImageStreamerView();
    }

    @Override
    protected ProgressListener getProgressListener(LoadImageActionModel actionModel) {
        return actionModel.getImageStreamerView().getLoadingIndicator();
    }

    @Override
    protected Worker<LoadWorkerModel<UpdatableImage>, UpdatableImage, ProgressModel>
    createWorker(LoadImageActionModel actionModel) {
        return new LoadImageWorker(actionModel);
    }

}
