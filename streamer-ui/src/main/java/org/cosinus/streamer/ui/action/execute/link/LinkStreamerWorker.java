/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.ui.action.execute.link;

import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.swing.worker.PipelineWorker;
import org.cosinus.swing.worker.WorkerModel;

import java.nio.file.Path;
import java.util.stream.Stream;

public class LinkStreamerWorker extends PipelineWorker<WorkerModel<Streamer<?>>, Streamer<?>, StreamersProgressModel> {

    private final LinkStreamersModel linkStreamersModel;

    public LinkStreamerWorker(final LinkStreamersModel linkStreamersModel,
                              final WorkerModel<Streamer<?>> workerModel) {
        super(linkStreamersModel, workerModel, new StreamersProgressModel());
        this.linkStreamersModel = linkStreamersModel;
    }

    @Override
    public Stream<Streamer<?>> openPipelineInputStream(PipelineStrategy pipelineStrategy) {
        return linkStreamersModel.getStreamersToLink().stream();
    }

    @Override
    protected StreamConsumer<Streamer<?>> streamConsumer() {
        return streamer -> {
            Path linkPath = linkStreamersModel.getDestination()
                .resolveStreamerPath(linkStreamersModel.getSource(), streamer);
            linkStreamersModel.getDestination().createLink(linkPath, streamer.getPath());
        };
    }
}
