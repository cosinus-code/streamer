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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.api.pack.PackerHandler;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.ui.action.execute.PipelineWorker;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerViewStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * {@link javax.swing.SwingWorker} for loading a streamer
 */
public class LoadStreamerWorker<T> extends PipelineWorker<StreamedContent<T>, T> {

    private static final Logger LOG = LogManager.getLogger(LoadStreamerWorker.class);

    @Autowired
    private StreamerViewStorage streamerViewStorage;

    @Autowired
    private StreamerHandler streamerHandler;

    @Autowired
    private PackerHandler packerHandler;

    private final PanelLocation panelLocation;

    public LoadStreamerWorker(LoadActionModel loadActionModel) {
        super(loadActionModel.getActionId(),
            new StreamedContent<>(
                loadActionModel.getStreamer(),
                loadActionModel.getContentIdentifier()));
        this.panelLocation = loadActionModel.getView().getCurrentLocation();
    }

    @Override
    public Stream<T> openPipelineInputStream(PipelineStrategy pipelineStrategy)
    {
        return (Stream<T>) workerModel.getStreamer().stream();
    }

    @Override
    public StreamConsumer<T> openPipelineOutputStream(PipelineStrategy pipelineStrategy)
    {
        return item -> {
            try
            {
                sleep(100);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            publish(item);
        };
        //return this::publish;
    }

    @Override
    public void preparePipelineOpen(PipelineStrategy pipelineStrategy, PipelineListener<T> pipelineListener)
    {
        workerModel.setStreamer(prepareStreamerToLoad(workerModel.getStreamer()));
        if (workerModel.getStreamer() == null) {
            LOG.trace("No streamer to load.");
            cancel();
        }
    }

    private Streamer prepareStreamerToLoad(Streamer streamerToLoad) {
        return ofNullable(streamerToLoad)
            .or(this::loadLastStreamer)
            .or(() -> ofNullable(streamerHandler.getDefaultStreamer()))
            .map(this::checkIfStreamerExist)
            .map(this::checkIfStreamerIsPacked)
            .orElse(null);
    }

    private Optional<Streamer> loadLastStreamer() {
        return streamerViewStorage.loadLastLoadedStreamer(panelLocation)
            .map(urlPath -> streamerHandler.getStreamer(urlPath));
    }

    private Streamer checkIfStreamerExist(Streamer streamerToCheck) {
        return ofNullable(streamerToCheck)
            .filter(not(Streamer::exists))
            .map(this::getFirstAncestorAlive)
            .orElse(streamerToCheck);
    }

    private Streamer checkIfStreamerIsPacked(Streamer streamerToCheck) {
        return ofNullable(streamerToCheck)
            .filter(streamer -> BinaryStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(BinaryStreamer.class::cast)
            .<Streamer>flatMap(binaryStream -> packerHandler
                .findPacker(binaryStream.getType())
                .map(packer -> packer.pack(binaryStream)))
            .orElse(streamerToCheck);
    }

    private Streamer getFirstAncestorAlive(Streamer streamer) {
        return ofNullable(streamer.getParent())
            .filter(not(Streamer::exists))
            .map(this::getFirstAncestorAlive)
            .orElse(streamer.getParent());
    }
}
