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
import org.cosinus.streamer.api.TextStreamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.api.pack.PackerHandler;
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
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
import static org.cosinus.streamer.ui.view.text.TextStreamerView.TEXT_EDITOR;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadWorker}
 */
@Component
public class LoadActionExecutor implements ActionExecutor<LoadActionModel> {

    private static final Logger LOG = LogManager.getLogger(LoadActionExecutor.class);

    private final StreamerViewStorage streamerViewStorage;

    private final StreamerViewHandler streamerViewHandler;

    private final StreamerHandler streamerHandler;

    private final PackerHandler packerHandler;

    protected final WorkerListenerHandler workerListenerHandler;

    private final Map<String, LoadWorker<?>> workersMap;

    public LoadActionExecutor(StreamerViewStorage streamerViewStorage,
                              StreamerViewHandler streamerViewHandler,
                              StreamerHandler streamerHandler,
                              PackerHandler packerHandler,
                              WorkerListenerHandler workerListenerHandler) {
        this.streamerViewStorage = streamerViewStorage;
        this.streamerViewHandler = streamerViewHandler;
        this.streamerHandler = streamerHandler;
        this.packerHandler = packerHandler;
        this.workerListenerHandler = workerListenerHandler;
        this.workersMap = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(LoadActionModel actionModel) {
        startLoadWorker(actionModel);
    }

    private <V> void startLoadWorker(LoadActionModel actionModel) {
        Streamer<V> streamerToLoad =
            prepareStreamerToLoad(actionModel.getStreamerToLoad(), actionModel.getLocationToLoadTo());
        if (streamerToLoad == null) {
            return;
        }

        String streamerViewNameToOpen = ofNullable(actionModel.getStreamerViewNameToLoadIn())
            .filter(viewName -> !streamerToLoad.isTextCompatible() || TEXT_EDITOR.equals(viewName))
            .orElseGet(() -> streamerToLoad.isTextCompatible() ? TEXT_EDITOR : null);

        StreamerView<V> streamerViewToLoadTo = streamerViewHandler
            .loadStreamerView(actionModel.getLocationToLoadTo(), streamerViewNameToOpen, streamerToLoad);
        if (streamerViewToLoadTo == null) {
            return;
        }
        String itemToSelectAfterLoad = actionModel.getItemToSelectAfterLoad();
        LoadWorker<V> worker = new LoadWorker<>(
            actionModel.getActionId(), streamerToLoad, streamerViewToLoadTo, itemToSelectAfterLoad);
        workerListenerHandler.register(worker.getId(), streamerViewToLoadTo);
        registerWorker(worker);
        worker.start();
    }

    private <V> void registerWorker(LoadWorker<V> worker) {
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

    private Streamer prepareStreamerToLoad(Streamer streamerToLoad, PanelLocation location) {
        return ofNullable(streamerToLoad)
            .or(() -> loadLastStreamer(location))
            .or(() -> ofNullable(streamerHandler.getDefaultStreamer()))
            .map(this::checkIfStreamerExist)
            .map(this::checkIfStreamerIsPacked)
            .map(this::checkIfStreamerIsText)
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
        return streamerToCheck.binaryStreamer() != null && streamerToCheck.isTextCompatible() ?
            new TextStreamer(streamerToCheck.binaryStreamer()) :
            streamerToCheck;
    }
}
