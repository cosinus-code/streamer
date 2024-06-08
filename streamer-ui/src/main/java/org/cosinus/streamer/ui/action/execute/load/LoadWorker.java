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

package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.ui.action.execute.PipelineWorker;
import org.cosinus.streamer.ui.view.StreamerView;

import java.util.stream.Stream;

/**
 * {@link javax.swing.SwingWorker} for loading a streamer
 */
public class LoadWorker<T> extends PipelineWorker<LoadWorkerModel<T>, T> implements PipelineListener<T> {

    private final Streamer<T> streamerToLoad;

    private final StreamerView<T> streamerViewToLoadTo;

    public LoadWorker(String id,
                      final Streamer<T> streamerToLoad,
                      final StreamerView<T> streamerViewToLoadTo,
                      String contentIdentifier) {
        super(id, streamerViewToLoadTo.getLoadWorkerModel());
        this.streamerToLoad = streamerToLoad;
        this.streamerViewToLoadTo = streamerViewToLoadTo;
        workerModel.setContentIdentifier(contentIdentifier);
    }

    @Override
    public void preparePipelineOpen(PipelineStrategy pipelineStrategy, PipelineListener<T> pipelineListener) {
        streamerToLoad.init();
    }

    @Override
    public void beforePipelineOpen() {
        streamerViewToLoadTo.reset(streamerToLoad);
    }

    @Override
    public Stream<T> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return streamerToLoad.stream();
    }

    @Override
    public PipelineListener<T> getPipelineListener() {
        return this;
    }

    @Override
    protected StreamConsumer<T> streamConsumer() {
        return null;
    }
}
