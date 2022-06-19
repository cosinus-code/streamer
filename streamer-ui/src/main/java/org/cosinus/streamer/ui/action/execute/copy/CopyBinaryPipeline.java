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
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipeline;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipelineStrategy;
import org.cosinus.streamer.api.stream.pipeline.error.SkipPipelineConsumeException;
import org.cosinus.streamer.ui.action.execute.SwingProgressWorker;
import org.cosinus.streamer.ui.action.progress.CopyProgressModel;

import java.io.InputStream;
import java.io.OutputStream;

public class CopyBinaryPipeline implements BinaryPipeline {

    private final BinaryStreamer source;

    private BinaryStreamer target;

    private final CopyBinaryStrategy copyStrategy;

    private final PipelineListener<byte[]> pipelineListener;

    public CopyBinaryPipeline(BinaryStreamer source,
                              BinaryStreamer target,
                              CopyStrategy copyStrategy,
                              SwingProgressWorker<CopyProgressModel> progressWorker) {
        this.source = source;
        this.target = target;
        this.copyStrategy = new CopyBinaryStrategy(source, target, copyStrategy, progressWorker);
        this.pipelineListener = new CopyBinaryListener(source, target, progressWorker);
    }

    @Override
    public BinaryPipelineStrategy getPipelineStrategy() {
        return copyStrategy;
    }

    @Override
    public PipelineListener<byte[]> getPipelineListener() {
        return pipelineListener;
    }

    @Override
    public InputStream inputStream() {
        return source.inputStream();
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return target.outputStream(append);
    }

    @Override
    public long outputSize() {
        return target.getSize();
    }

    @Override
    public void preparePipelineOpen(BinaryPipelineStrategy pipelineStrategy,
                                    PipelineListener<byte[]> pipelineListener) {
        target = copyStrategy.prepareTarget();
    }
}
