/*
 * Copyright 2022 Cosinus Software
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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.ui.action.execute.ProgressWorker;

public class CopyBinaryListener implements PipelineListener<byte[]> {

    private final BinaryStreamer source;

    private final BinaryStreamer target;

    private final ProgressWorker<CopyProgressModel> progressWorker;

    private final CopyProgressModel progressModel;

    public CopyBinaryListener(
        BinaryStreamer source, BinaryStreamer target, ProgressWorker<CopyProgressModel> progressWorker) {
        this.source = source;
        this.target = target;
        this.progressWorker = progressWorker;
        this.progressModel = progressWorker.getProgressModel();
    }

    @Override
    public void beforePipelineOpen() {
        progressWorker.publishProgress(() -> progressModel.startStreamerProgress(source, target));
    }

    @Override
    public void afterPipelineDataConsume(final byte[] bytes) {
        progressWorker.checkWorkerStatus();
        progressWorker.publishProgress(() -> progressModel.updateStreamerProgress(bytes.length));
    }

    @Override
    public void afterPipelineDataSkip(long skippedDataSize) {
        progressWorker.publishProgress(() -> {
            progressModel.updateStreamerProgress(skippedDataSize);
            progressModel.finishStreamerProgress();
        });
    }

    @Override
    public void afterPipelineClose() {
        progressWorker.publishProgress(progressModel::finishStreamerProgress);
    }
}
