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
package org.cosinus.streamer.ui.action.execute.find;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.worker.Worker;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.FindStreamerAction.FIND_STREAMER_ACTION_ID;

@Component
public class FindActionExecutor extends WorkerExecutor<FindActionModel, FindWorkerModel, Streamer<?>> {

    private final StreamerViewHandler streamerViewHandler;

    protected FindActionExecutor(final WorkerListenerHandler workerListenerHandler,
                                 final StreamerViewHandler streamerViewHandler) {
        super(workerListenerHandler);
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected WorkerListener<FindWorkerModel, Streamer<?>> createWorkerListener(FindActionModel actionModel) {
        return streamerViewHandler.getView(actionModel.getLocation())
            .map(StreamerView::getLoadingIndicator)
            .map(loaderIndicator -> new WorkerListener<FindWorkerModel, Streamer<?>>() {
                @Override
                public void workerStarted(FindWorkerModel workerModel) {
                    loaderIndicator.startLoading();
                }

                @Override
                public void workerFinished(FindWorkerModel workerModel) {
                    loaderIndicator.finishLoading();
                    actionModel.streamerConsumer().accept(workerModel.getFoundStreamer());
                }
            })
            .orElse(null);
    }

    @Override
    protected Worker<FindWorkerModel, Streamer<?>> createWorker(FindActionModel actionModel) {
        return new FindWorker(actionModel);
    }

    @Override
    public String getHandledAction() {
        return FIND_STREAMER_ACTION_ID;
    }
}
