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

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.PipelineWorker;

import java.util.stream.Stream;

public class SaveWorker<T> extends PipelineWorker<SaveWorkerModel<T>, T> {
    private StreamConsumer<T> streamConsumer;

    public SaveWorker(final SaveActionModel<?> actionModel, final SaveWorkerModel<T> workerModel) {
        super(actionModel, workerModel);
    }

    @Override
    public Stream<T> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return workerModel.streamToSave();
    }

    @Override
    protected StreamConsumer<T> streamConsumer() {
        if (streamConsumer == null) {
            streamConsumer = workerModel.streamConsumer();
        }
        return streamConsumer;
    }

    @Override
    public PipelineListener<T> getPipelineListener() {
        return new PipelineListener<T>() {
            @Override
            public void afterPipelineClose(boolean pipelineFailed) {
                if (streamConsumer != null) {
                    streamConsumer.afterClose(pipelineFailed);
                }
            }
        };
    }
}
