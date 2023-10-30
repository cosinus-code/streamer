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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.Pipeline;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.error.AbortPipelineConsumeException;
import org.cosinus.streamer.ui.action.execute.ProgressWorker;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static org.cosinus.streamer.api.stream.FlatStreamingStrategy.LEVEL_BOTTOM_UP;

/**
 * {@link ProgressWorker} for deleting streamers
 */
public class DeleteWorker extends ProgressWorker<StreamersProgressModel>
    implements Pipeline<Streamer<?>, Stream<Streamer<?>>, StreamConsumer<Streamer<?>>, DeleteStrategy> {

    @Autowired
    protected DialogHandler dialogHandler;

    private final DeleteActionModel deleteModel;

    private final DeleteStrategy deleteStrategy;

    private final DeleteListener deleteListener;

    private long streamersToDeleteCount;

    public DeleteWorker(Window parentWindow,
                        DeleteActionModel deleteModel) {
        super(parentWindow,
            deleteModel.getActionId(),
            new StreamersProgressModel(deleteModel.getActionId()));
        this.deleteModel = deleteModel;
        this.deleteStrategy = new DeleteStrategy();
        this.deleteListener = new DeleteListener();
    }

    @Override
    protected void doWork() {
        try {
            consume();
        } catch (AbortPipelineConsumeException ex) {
            throw new AbortActionException("Delete pipeline aborted", ex);
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act-delete-error");
        }
    }

    @Override
    public Stream<Streamer<?>> openPipelineInputStream(DeleteStrategy pipelineStrategy)
    {
        return deleteModel.getStreamer().flatStream(LEVEL_BOTTOM_UP, deleteModel.getStreamerFilter());
    }

    @Override
    public StreamConsumer<Streamer<?>> openPipelineOutputStream(DeleteStrategy pipelineStrategy)
    {
        return streamerToDelete -> {
            if (streamerToDelete.exists() && !streamerToDelete.delete()) {
                throw new ActionException("act-delete-cannot", streamerToDelete.getPath());
            }
        };
    }

    @Override
    public DeleteStrategy getPipelineStrategy()
    {
        return deleteStrategy;
    }

    @Override
    public PipelineListener<Streamer<?>> getPipelineListener()
    {
        return deleteListener;
    }

    @Override
    public void preparePipelineOpen(DeleteStrategy pipelineStrategy, PipelineListener<Streamer<?>> pipelineListener)
    {
        streamersToDeleteCount = deleteModel
            .getStreamer()
            .count(deleteModel.getStreamerFilter());

    }

    private class DeleteListener implements PipelineListener<Streamer<?>> {
        @Override
        public void beforePipelineOpen()
        {
            publishProgress(() -> progress.startProgress(streamersToDeleteCount));
        }

        @Override
        public void beforePipelineDataConsume(Streamer<?> data)
        {
            checkWorkerStatus();
            publishProgress(() -> progress.updateProgress(data));
        }

        @Override
        public void afterPipelineClose()
        {
            publishProgress(progress::finishProgress);
        }
    }
}
