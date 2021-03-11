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
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadStreamerWorker}
 */
@Component
public class LoadElementWorkerExecutor<P extends ProgressModel> implements ActionExecutor<LoadActionModel> {

    private static final Logger LOG = LogManager.getLogger(LoadElementWorkerExecutor.class);

    private final PackerHandler packerHandler;

    protected final ProgressListenerHandler<P> progressListenerHandler;

    private final Map<String, LoadStreamerWorker> workersMap;

    public LoadElementWorkerExecutor(PackerHandler packerHandler,
                                     ProgressListenerHandler<P> progressListenerHandler) {
        this.packerHandler = packerHandler;
        this.progressListenerHandler = progressListenerHandler;
        this.workersMap = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(final LoadActionModel loadActionModel) {
        cancel(loadActionModel.getActionId());

        if (loadActionModel.getStreamer() == null) {
            LOG.trace("Cannot load a null element -> ignore the command.");
            return;
        }


        Streamer streamerToLoad = Optional.of(loadActionModel.getStreamer())
            .filter(streamer -> InputStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(InputStreamer.class::cast)
            .<Streamer>flatMap(inputStream -> packerHandler
                .findPacker(loadActionModel.getStreamer().getType())
                .map(packer -> packer.pack(inputStream)))
            .orElseGet(loadActionModel::getStreamer);


        if (streamerToLoad.isDirectory()) {
            progressListenerHandler.register(loadActionModel.getView());
            LoadStreamerWorker worker = new LoadStreamerWorker(streamerToLoad,
                                                               loadActionModel);
            workersMap.put(loadActionModel.getActionId(), worker);
            worker.execute();
            progressListenerHandler.startProgress();
        }
    }

    @Override
    public void cancel(final String actionId) {
        Optional.ofNullable(workersMap.get(actionId))
            .ifPresent(SwingWorker::cancel);
    }

    @Override
    public String getHandledAction() {
        return LoadActionModel.class.getName();
    }
}
