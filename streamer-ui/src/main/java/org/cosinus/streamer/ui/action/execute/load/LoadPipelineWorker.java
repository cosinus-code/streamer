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

package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.progress.ProgressListener;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.StreamWorker;
import org.cosinus.swing.worker.WorkerListener;

/**
 * {@link javax.swing.SwingWorker} for loading a streamer
 */
public class LoadPipelineWorker<T> extends StreamWorker<LoadWorkerModel<T>, T, ProgressModel> {

    private final Streamer<T> streamerToLoad;

    private final StreamerView<T> streamerViewToLoadTo;

    public LoadPipelineWorker(LoadActionModel<T> actionModel) {
        super(actionModel,
            actionModel.getStreamerViewToLoadTo().getLoadWorkerModel(),
            actionModel.getStreamerToLoad(),
            actionModel.getStreamerViewToLoadTo().getLoadingIndicator().getProgressModel());

        this.streamerToLoad = actionModel.getStreamerToLoad();
        this.streamerViewToLoadTo = actionModel.getStreamerViewToLoadTo();
    }

    @Override
    public void beforePipelineOpen() {
        streamerToLoad.init();
        streamerViewToLoadTo.reset(streamerToLoad);
    }

    @Override
    public void afterPipelineOpen() {
        progressModel.startProgress(workerModel.getTotalSizeToLoad());
    }

    @Override
    public void afterPipelineDataConsume(T data) {
        progressModel.updateProgress(workerModel.getLoadedSize(), workerModel.getTotalSizeToLoad());
    }

    @Override
    public void afterPipelineClose(boolean pipelineFailed) {
        progressModel.finishProgress();
    }

    @Override
    public LoadPipelineWorker<T> registerListener(WorkerListener<LoadWorkerModel<T>, T> workerListener) {
        super.registerListener(workerListener);
        return this;
    }

    @Override
    public LoadPipelineWorker<T> registerListener(ProgressListener<ProgressModel> progressListener) {
        super.registerListener(progressListener);
        return this;
    }
}
