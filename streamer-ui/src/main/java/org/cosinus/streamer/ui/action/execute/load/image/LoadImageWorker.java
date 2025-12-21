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

package org.cosinus.streamer.ui.action.execute.load.image;

import org.cosinus.stream.pipeline.PipelineListener;
import org.cosinus.stream.pipeline.binary.BinaryPipeline;
import org.cosinus.stream.pipeline.binary.BinaryPipelineStrategy;
import org.cosinus.stream.pipeline.binary.BinaryStreamConsumer;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.Worker;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.swing.image.UpdatableImage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import static org.cosinus.swing.format.FormatHandler.MEGA_INT;

public class LoadImageWorker extends Worker<LoadWorkerModel<UpdatableImage>, UpdatableImage, ProgressModel>
    implements BinaryPipeline, PipelineListener<byte[]>, BinaryPipelineStrategy {

    public static final int IMAGE_LOAD_RATE = 3 * MEGA_INT;

    private final BinaryStreamer streamerToLoad;

    private final ImageStreamerView imageStreamerView;

    public LoadImageWorker(final LoadImageActionModel actionModel) {
        super(actionModel,
            actionModel.getImageStreamerView().getLoadWorkerModel(),
            actionModel.getImageStreamerView().getLoadingIndicator().getProgressModel());
        this.streamerToLoad = actionModel.getStreamerToLoad();
        this.imageStreamerView = actionModel.getImageStreamerView();
    }

    @Override
    protected void doWork() {
        try {
            openPipeline();
        } catch (IOException | UncheckedIOException ex) {
            throw new ActionException(ex, "act-load-error");
        }
    }

    @Override
    public void beforePipelineOpen() {
        imageStreamerView.reset(streamerToLoad);
    }

    @Override
    public void preparePipelineOpen(final BinaryPipelineStrategy pipelineStrategy,
                                    final PipelineListener<byte[]> pipelineListener) {
        streamerToLoad.init();
    }

    @Override
    public PipelineListener<byte[]> getPipelineListener() {
        return this;
    }

    @Override
    public BinaryPipelineStrategy getPipelineStrategy() {
        return this;
    }

    @Override
    public BinaryStreamConsumer openPipelineOutputStream(BinaryPipelineStrategy pipelineStrategy) {
        return new ImageLoader(this);
    }

    @Override
    public void afterPipelineClose(boolean pipelineFailed) {
        streamerToLoad.finalizeStreaming();
    }

    @Override
    public InputStream inputStream() {
        return streamerToLoad.binaryStreamer().inputStream();
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return null;
    }

    @Override
    public long outputSize() {
        return streamerToLoad.getSize();
    }

    public Streamer<byte[]> getStreamerToLoad() {
        return streamerToLoad;
    }

    @Override
    public int getPipelineRate() {
        return IMAGE_LOAD_RATE;
    }
}
