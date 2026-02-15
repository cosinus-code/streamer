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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.pipeline.PipelineListener;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.DefaultPipelineStrategy;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.worker.PipelineWorker;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static org.cosinus.stream.FlatStreamingStrategy.LEVEL_BOTTOM_UP;

/**
 * {@link Worker} for deleting streamers
 */
public class DeleteWorker
    extends PipelineWorker<WorkerModel<Streamer<?>>, Streamer<?>, StreamersProgressModel> {

    @Autowired
    protected DialogHandler dialogHandler;

    @Autowired
    protected Translator translator;

    private final DeleteActionModel deleteModel;

    private final PipelineStrategy pipelineStrategy;

    public DeleteWorker(DeleteActionModel deleteModel, WorkerModel<? extends Streamer<?>> workerModel) {
        super(deleteModel, (WorkerModel<Streamer<?>>) workerModel, new StreamersProgressModel());
        this.deleteModel = deleteModel;
        this.pipelineStrategy = new DefaultPipelineStrategy();
    }

    @Override
    public Stream<Streamer<?>> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return deleteModel
            .source()
            .flatStream(LEVEL_BOTTOM_UP, this.pipelineStrategy, deleteModel.getStreamerFilter());
    }

    @Override
    protected StreamConsumer<Streamer<?>> streamConsumer() {
        return streamerToDelete -> {
            if (streamerToDelete.exists() && !streamerToDelete.delete(deleteModel.isMoveToTrash())) {
                throw new ActionException("act-delete-cannot", streamerToDelete.getPath());
            }
        };
    }

    @Override
    public void preparePipelineOpen(PipelineStrategy pipelineStrategy, PipelineListener<Streamer<?>> pipelineListener) {
        StreamerFilter streamerFilter = deleteModel.getStreamerFilter();
        try (Stream<? extends Streamer<?>> flatStream =
                 deleteModel.source().flatStream(this.pipelineStrategy, streamerFilter)) {

            updateProgress(progress ->
                progress.addTotalProgress(flatStream.count()));
        }
    }

    @Override
    public void beforePipelineDataConsume(final Streamer<?> streamerToDelete) {
        progressModel.setCurrentStreamer(streamerToDelete);
    }

    @Override
    public void afterPipelineDataConsume(final Streamer<?> streamerToDelete) {
        progressModel.addProgress(1);
    }

    @Override
    public PipelineStrategy getPipelineStrategy() {
        return pipelineStrategy;
    }
}
