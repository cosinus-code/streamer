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

import org.cosinus.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.swing.worker.SimpleWorker;

public class CopyBinaryListener implements PipelineListener<byte[]> {

    private final BinaryStreamer source;

    private final BinaryStreamer target;

    private final SimpleWorker<CopyProgressModel> copyWorker;

    private final CopyProgressModel workerModel;

    public CopyBinaryListener(
        BinaryStreamer source, BinaryStreamer target, SimpleWorker<CopyProgressModel> copyWorker) {
        this.source = source;
        this.target = target;
        this.copyWorker = copyWorker;
        this.workerModel = copyWorker.getWorkerModel();
    }

    @Override
    public void beforePipelineOpen() {
        copyWorker.updateModel(() -> workerModel.startStreamerProgress(source, target));
    }

    @Override
    public void afterPipelineDataConsume(final byte[] bytes) {
        copyWorker.updateModel(() -> workerModel.updateStreamerProgress(bytes.length));
    }

    @Override
    public void afterPipelineDataSkip(long skippedDataSize) {
        copyWorker.updateModel(() -> {
            workerModel.updateStreamerProgress(skippedDataSize);
            workerModel.finishStreamerProgress();
        });
    }

    @Override
    public void afterPipelineClose(boolean pipelineFailed) {
        source.finalizeStreaming();
        target.finalizeStreaming();
        target.finalizeCopy(source);
        copyWorker.updateModel(workerModel::finishStreamerProgress);
    }
}
