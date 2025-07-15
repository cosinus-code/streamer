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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.NoPipelineStrategy;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.stream.pipeline.StreamPipeline;
import org.cosinus.streamer.api.stream.pipeline.error.AbortPipelineConsumeException;
import org.cosinus.streamer.api.worker.SimpleWorker;
import org.cosinus.streamer.api.worker.Worker;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import error.AbortActionException;
import error.ActionException;
import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static org.cosinus.streamer.api.stream.FlatStreamingStrategy.LEVEL_BOTTOM_UP;

/**
 * {@link Worker} for deleting streamers
 */
public class DeleteWorker extends SimpleWorker<StreamersProgressModel>
    implements StreamPipeline<Streamer<?>>
{

    @Autowired
    protected DialogHandler dialogHandler;

    private final DeleteActionModel deleteModel;

    private final DeleteListener deleteListener;

    private long streamersToDeleteCount;

    public DeleteWorker(DeleteActionModel deleteModel) {
        super(deleteModel, new StreamersProgressModel());
        this.deleteModel = deleteModel;
        this.deleteListener = new DeleteListener();
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (AbortPipelineConsumeException ex) {
            throw new AbortActionException("Delete pipeline aborted", ex);
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act-delete-error");
        }
    }

    @Override
    public Stream<Streamer<?>> openPipelineInputStream(PipelineStrategy pipelineStrategy)
    {
        return deleteModel.getParentStreamer().flatStream(LEVEL_BOTTOM_UP, deleteModel.getStreamerFilter());
    }

    @Override
    public StreamConsumer<Streamer<?>> openPipelineOutputStream(PipelineStrategy pipelineStrategy)
    {
        return streamerToDelete -> {
            if (streamerToDelete.exists() && !streamerToDelete.delete(deleteModel.isMoveToTrash())) {
                throw new ActionException("act-delete-cannot", streamerToDelete.getPath());
            }
        };
    }

    @Override
    public PipelineStrategy getPipelineStrategy()
    {
        return new NoPipelineStrategy();
    }

    @Override
    public PipelineListener<Streamer<?>> getPipelineListener()
    {
        return deleteListener;
    }

    @Override
    public void preparePipelineOpen(PipelineStrategy pipelineStrategy, PipelineListener<Streamer<?>> pipelineListener)
    {
        ParentStreamer<Streamer<?>> parentStreamer = deleteModel.getParentStreamer();
        StreamerFilter streamerFilter = deleteModel.getStreamerFilter();
        try (Stream<? extends Streamer<?>> flatStream = parentStreamer.flatStream(streamerFilter)) {
            flatStream.forEach(streamer ->
                pipelineListener.onPreparingPipeline(++streamersToDeleteCount));
        }
    }

    private class DeleteListener implements PipelineListener<Streamer<?>> {
        @Override
        public void onPreparingPipeline(long preparedDataSize) {
            updateModel(() -> workerModel.setProgressTotalSize(streamersToDeleteCount));
        }

        @Override
        public void beforePipelineOpen()
        {
            updateModel(() -> workerModel.startProgress(streamersToDeleteCount));
        }

        @Override
        public void beforePipelineDataConsume(Streamer<?> data)
        {
            updateModel(() -> workerModel.updateProgress(data));
        }

        @Override
        public void afterPipelineClose(boolean pipelineFailed)
        {
            updateModel(workerModel::finishProgress);
        }
    }
}
