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
import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.progress.CopyProgressModel;

public class CopyBinaryListener implements PipelineListener<byte[]> {

    private final BinaryStreamer source;

    private final BinaryStreamer target;

    private final SwingProgressWorker<CopyProgressModel> progressWorker;

    private final CopyProgressModel progressModel;

    public CopyBinaryListener(
        BinaryStreamer source, BinaryStreamer target, SwingProgressWorker<CopyProgressModel> progressWorker) {
        this.source = source;
        this.target = target;
        this.progressWorker = progressWorker;
        this.progressModel = progressWorker.getSwingProgress();
    }

    @Override
    public void beforePipelineOpen() {
        progressModel.startStreamerProgress(source, target);
        progressWorker.publishProgress();
    }

    @Override
    public void afterPipelineDataConsume(byte[] bytes) {
        progressWorker.checkWorkerStatus();
        progressModel.updateStreamerProgress(bytes.length);
        progressWorker.publishProgress();
    }

    @Override
    public void afterPipelineDataSkip(long skippedDataSize) {
        progressModel.updateStreamerProgress(skippedDataSize);
        progressModel.finishStreamerProgress();
        progressWorker.publishProgress();
    }

    @Override
    public void afterPipelineClose() {
        progressModel.finishStreamerProgress();
        progressWorker.publishProgress();
    }
}
