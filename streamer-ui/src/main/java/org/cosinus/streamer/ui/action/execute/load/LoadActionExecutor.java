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
import org.cosinus.streamer.ui.action.execute.WorkerListener;
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.WorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ActionExecutor} based on {@link LoadStreamerWorker}
 */
@Component
public class LoadActionExecutor<M extends WorkerModel> implements ActionExecutor<LoadActionModel> {

    private static final Logger LOG = LogManager.getLogger(LoadActionExecutor.class);

    private final StreamerViewHandler streamerViewHandler;

    protected final WorkerListenerHandler<M> workerListenerHandler;

    private final Map<String, LoadStreamerWorker> workersMap;

    public LoadActionExecutor(StreamerViewHandler streamerViewHandler,
                              WorkerListenerHandler<M> workerListenerHandler) {
        this.streamerViewHandler = streamerViewHandler;
        this.workerListenerHandler = workerListenerHandler;
        this.workersMap = new ConcurrentHashMap<>();
    }

    @Override
    public void execute(final LoadActionModel loadActionModel) {
        LoadStreamerWorker worker = new LoadStreamerWorker(loadActionModel);
        workerListenerHandler.register(worker.getId(), loadActionModel.getView());
        registerWorker(worker);
        worker.start();
    }

    private void registerWorker(LoadStreamerWorker worker) {
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
}
