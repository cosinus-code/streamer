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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SimpleWorker;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.progress.ProgressFormHandler;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.CopyStreamerAction.COPY_STREAMER_ACTION_ID;

/**
 * Implementation of {@link ActionExecutor} for copying streamers based on {@link CopyWorker}
 */
@Component
public class CopyWorkerExecutor<S extends Streamer<S>, T extends Streamer<T>>
    extends AbstractCopyWorkerExecutor<S, T, CopyActionModel<S, T>> {

    protected CopyWorkerExecutor(final ProgressFormHandler progressFormHandler,
                                 final WorkerListenerHandler workerListenerHandler,
                                 final LoadActionExecutor loadActionExecutor,
                                 final StreamerViewHandler streamerViewHandler) {
        super(progressFormHandler, workerListenerHandler, loadActionExecutor, streamerViewHandler);
    }

    @Override
    protected SimpleWorker<CopyProgressModel> createWorker(final CopyActionModel<S, T> actionModel) {
        return new CopyWorker<>(actionModel);
    }

    @Override
    public String getHandledAction() {
        return COPY_STREAMER_ACTION_ID;
    }
}
