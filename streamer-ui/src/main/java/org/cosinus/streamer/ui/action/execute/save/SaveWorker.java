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

import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.swing.progress.ProgressListener;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.PipelineWorker;
import org.cosinus.swing.worker.WorkerListener;

import java.util.stream.Stream;

public class SaveWorker<T> extends PipelineWorker<SaveWorkerModel<T>, T, ProgressModel> {

    private final SaveWorkerModel<T> saveModel;

    public SaveWorker(final SaveActionModel<?> actionModel, final SaveWorkerModel<T> saveModel) {
        super(actionModel, saveModel, new ProgressModel());
        this.saveModel = saveModel;
    }

    @Override
    public Stream<T> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return saveModel.streamToSave();
    }

    @Override
    protected StreamConsumer<T> streamConsumer() {
        return saveModel.saver();
    }

    @Override
    public SaveWorker<T> registerListener(WorkerListener<SaveWorkerModel<T>, T> workerListener) {
        super.registerListener(workerListener);
        return this;
    }

    @Override
    public SaveWorker<T> registerListener(ProgressListener<ProgressModel> progressListener) {
        super.registerListener(progressListener);
        return this;
    }
}
