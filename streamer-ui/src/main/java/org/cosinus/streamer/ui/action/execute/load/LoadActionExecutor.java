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
import org.cosinus.streamer.api.InputStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.pack.PackerHandler;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.action.progress.ProgressModel;
import org.cosinus.streamer.ui.view.AddressBar;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadStreamerWorker}
 */
@Component
public class LoadActionExecutor<P extends ProgressModel> implements ActionExecutor<LoadActionModel> {

    private static final Logger LOG = LogManager.getLogger(LoadActionExecutor.class);

    private final PackerHandler packerHandler;

    private final StreamerViewHandler streamerViewHandler;

    protected final ProgressListenerHandler<P> progressListenerHandler;

    private final Map<String, LoadStreamerWorker> workersMap;

    private final AddressBar addressBar;

    public LoadActionExecutor(PackerHandler packerHandler,
                              StreamerViewHandler streamerViewHandler,
                              ProgressListenerHandler<P> progressListenerHandler,
                              AddressBar addressBar) {
        this.packerHandler = packerHandler;
        this.streamerViewHandler = streamerViewHandler;
        this.progressListenerHandler = progressListenerHandler;
        this.workersMap = new ConcurrentHashMap<>();
        this.addressBar = addressBar;
    }

    @Override
    public void execute(final LoadActionModel loadActionModel) {
        cancel(loadActionModel.getActionId());

        Streamer aliveStreamer = ofNullable(loadActionModel.getStreamer())
            .filter(not(Streamer::exists))
            .map(this::getFirstAncestorAlive)
            .orElse(loadActionModel.getStreamer());

        if (aliveStreamer == null) {
            LOG.trace("Cannot load a null streamer -> ignore the command.");
            return;
        }

        Streamer streamerToLoad = Optional.of(aliveStreamer)
            .filter(streamer -> InputStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(InputStreamer.class::cast)
            .<Streamer>flatMap(inputStream -> packerHandler
                .findPacker(aliveStreamer.getType())
                .map(packer -> packer.pack(inputStream)))
            .orElse(aliveStreamer);

        if (streamerToLoad.isDirectory()) {
            streamerViewHandler.getPanel(loadActionModel.getView().getCurrentLocation())
                .ifPresent(panel -> ofNullable(streamerToLoad.getUrlPath())
                    .map(address -> address.split("://"))
                    .map(address -> address[address.length - 1])
                    .ifPresent(address -> {
                        addressBar.setAddress(address);
                        panel.setAddress(address);
                        panel.setFreeSpace(streamerToLoad.getFreeSpace(),
                                           streamerToLoad.getTotalSpace());
                    }));

            progressListenerHandler.register(loadActionModel.getActionId(), loadActionModel.getView());
            LoadStreamerWorker worker = new LoadStreamerWorker(streamerToLoad,
                                                               loadActionModel);
            workersMap.put(loadActionModel.getActionId(), worker);
            worker.execute();
            progressListenerHandler.startProgress(loadActionModel.getActionId());
        }
    }

    private Streamer getFirstAncestorAlive(Streamer streamer) {
        return ofNullable(streamer.getParent())
            .filter(not(Streamer::exists))
            .map(this::getFirstAncestorAlive)
            .orElse(streamer.getParent());
    }

    @Override
    public void cancel(final String actionId) {
        ofNullable(workersMap.get(actionId))
            .ifPresent(SwingWorker::cancel);
    }

    @Override
    public String getHandledAction() {
        return LoadActionModel.class.getName();
    }
}
