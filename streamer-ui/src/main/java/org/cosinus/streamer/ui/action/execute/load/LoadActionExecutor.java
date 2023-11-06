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
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.api.pack.PackerHandler;
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.WorkerModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.StreamerViewStorage;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadWorker}
 */
@Component
public class LoadActionExecutor<M extends WorkerModel<T>, T>
    implements ActionExecutor<LoadActionModel<T, Streamer<T>>> {

    private static final Logger LOG = LogManager.getLogger(LoadActionExecutor.class);

    private final StreamerViewStorage streamerViewStorage;

    private final StreamerViewHandler streamerViewHandler;

    private final StreamerHandler streamerHandler;

    private final PackerHandler packerHandler;

    protected final WorkerListenerHandler<M> workerListenerHandler;

    private final Map<String, LoadWorker<T>> workersMap;

    public LoadActionExecutor(StreamerViewStorage streamerViewStorage,
                              StreamerViewHandler streamerViewHandler,
                              StreamerHandler streamerHandler,
                              PackerHandler packerHandler,
                              WorkerListenerHandler<M> workerListenerHandler) {
        this.streamerViewStorage = streamerViewStorage;
        this.streamerViewHandler = streamerViewHandler;
        this.streamerHandler = streamerHandler;
        this.packerHandler = packerHandler;
        this.workerListenerHandler = workerListenerHandler;
        this.workersMap = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(final LoadActionModel<T, Streamer<T>> loadActionModel) {
        PanelLocation location = loadActionModel.getView().getCurrentLocation();
        Streamer streamerToLoad = prepareStreamerToLoad(loadActionModel.getStreamerToLoad(), location);
        StreamerView streamerViewToLoadTo = findStreamerToLoadTo(streamerToLoad, location);
        if (streamerViewToLoadTo == null) {
            return;
        }
        LoadWorker<T> worker = new LoadWorker<>(
            loadActionModel.getActionId(),
            streamerToLoad,
            streamerViewToLoadTo,
            loadActionModel.getContentIdentifier());

        workerListenerHandler.register(worker.getId(), streamerViewToLoadTo);
        registerWorker(worker);
        worker.start();
    }

    private void registerWorker(LoadWorker<T> worker) {
        cancel(worker.getId());
        workersMap.put(worker.getId(), worker);
    }

    @Override
    public void cancel(final String workerId) {
        ofNullable(workersMap.get(workerId))
            .ifPresent(SwingWorker::cancel);
    }

    @Override
    public void remove(String workerId)
    {
        workersMap.remove(workerId);
    }

    @Override
    public String getHandledAction() {
        return LoadActionModel.class.getName();
    }

    private <V> StreamerView<V> findStreamerToLoadTo(Streamer<V> streamerToLoad, PanelLocation location) {
        return streamerViewHandler.createStreamerView(streamerToLoad, location);
    }

    private Streamer prepareStreamerToLoad(Streamer streamerToLoad, PanelLocation location) {
        return ofNullable(streamerToLoad)
            .or(() -> loadLastStreamer(location))
            .or(() -> ofNullable(streamerHandler.getDefaultStreamer()))
            .map(this::checkIfStreamerExist)
            .map(this::checkIfStreamerIsPacked)
            //.map(this::checkIfStreamerIsText)
            .orElse(null);
    }

    private Optional<Streamer> loadLastStreamer(PanelLocation location) {
        return streamerViewStorage.loadLastLoadedStreamer(location)
            .map(urlPath -> streamerHandler.getStreamer(urlPath));
    }

    private Streamer<?> checkIfStreamerExist(Streamer streamerToCheck) {
        return ofNullable(streamerToCheck)
            .filter(not(Streamer::exists))
            .map(this::getFirstAncestorAlive)
            .orElse(streamerToCheck);
    }

    private Streamer<?> getFirstAncestorAlive(Streamer streamer) {
        return ofNullable(streamer.getParent())
            .filter(not(Streamer::exists))
            .map(this::getFirstAncestorAlive)
            .orElse(streamer.getParent());
    }

    private Streamer<?> checkIfStreamerIsPacked(Streamer<?> streamerToCheck) {
        return ofNullable(streamerToCheck.binaryStreamer())
            .<Streamer>flatMap(binaryStream -> packerHandler
                .findPacker(binaryStream.getType())
                .map(packer -> packer.pack(binaryStream)))
            .orElse(streamerToCheck);
    }

    private Streamer<?> checkIfStreamerIsText(Streamer<?> streamerToCheck) {
        Streamer<?> textStreamer = streamerToCheck.textStreamer();
        return textStreamer != null ? textStreamer : streamerToCheck;
    }
}
