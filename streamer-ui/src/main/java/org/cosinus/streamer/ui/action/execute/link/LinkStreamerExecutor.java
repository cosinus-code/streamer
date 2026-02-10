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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.progress.StreamersProgressModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.worker.WorkerExecutor;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.execute.link.LinkStreamersModel.LINK_STREAMER_ACTION_ID;

@Component
public class LinkStreamerExecutor
    extends WorkerExecutor<LinkStreamersModel, WorkerModel<Streamer<?>>, Streamer<?>, StreamersProgressModel> {

    private final StreamerViewHandler streamerViewHandler;

    public LinkStreamerExecutor(final StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    protected LinkStreamerWorker createWorker(LinkStreamersModel linkStreamersModel) {
        LinkStreamerWorker linkStreamerWorker = new LinkStreamerWorker(linkStreamersModel,
            (WorkerModel<Streamer<?>>) streamerViewHandler.getCurrentView().getCopyWorkerModel());
        linkStreamerWorker.registerListener(new WorkerListener<>() {
            @Override
            public void workerUpdated(WorkerModel<Streamer<?>> workerModel) {
                ofNullable(linkStreamersModel.getDestinationView())
                    .ifPresent(StreamerView::fireContentChanged);
            }

            @Override
            public void workerFinished(WorkerModel<Streamer<?>> workerModel) {
                ofNullable(linkStreamersModel.getDestinationView())
                    .ifPresent(StreamerView::reload);
            }
        });

        return linkStreamerWorker;
    }

    @Override
    public String getHandledAction() {
        return LINK_STREAMER_ACTION_ID;
    }
}
