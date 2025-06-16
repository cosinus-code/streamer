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
package org.cosinus.streamer.ui.action.execute.compute;

import org.cosinus.streamer.api.worker.*;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.action.ComputeStreamerSizeAction.COMPUTE_STREAMER_SIZE_ACTION_ID;

/**
 * Executor for computing a streamer size
 */
@Component
public class ComputeStreamerSizeExecutor
    extends WorkerExecutor<ComputeStreamerSizeModel, WorkerModel<Void>, Void> {

    protected ComputeStreamerSizeExecutor(WorkerListenerHandler workerListenerHandler) {
        super(workerListenerHandler);
    }

    @Override
    protected WorkerListener<WorkerModel<Void>, Void> createWorkerListener(
        ComputeStreamerSizeModel actionModel) {

        return null;
    }

    @Override
    protected Worker<WorkerModel<Void>, Void> createWorker(ComputeStreamerSizeModel actionModel) {
        return new ComputeStreamerSizeWorker(actionModel);
    }

    @Override
    public String getHandledAction() {
        return COMPUTE_STREAMER_SIZE_ACTION_ID;
    }
}
