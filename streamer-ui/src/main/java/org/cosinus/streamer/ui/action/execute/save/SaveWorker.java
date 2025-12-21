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
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.PipelineWorker;

import java.util.stream.Stream;

public class SaveWorker<T> extends PipelineWorker<SaveWorkerModel<T>, T, ProgressModel> {

    public SaveWorker(final SaveActionModel<?> actionModel, final SaveWorkerModel<T> workerModel) {
        super(actionModel, workerModel, new ProgressModel());
    }

    @Override
    public Stream<T> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return workerModel.streamToSave();
    }

    @Override
    protected StreamConsumer<T> streamConsumer() {
        return workerModel.streamConsumer();
    }
}
