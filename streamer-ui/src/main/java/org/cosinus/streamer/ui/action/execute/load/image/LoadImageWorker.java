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
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.swing.image.UpdatableImage;
import org.cosinus.swing.progress.ProgressModel;
import org.cosinus.swing.worker.StreamWorker;

import static org.cosinus.swing.format.FormatHandler.MEGA_INT;

/**
 * Worker which loads an image from the input stream of a streamer.
 * <p>
 * The pipeline input is the stream of the image updated after each chunk of {@link #IMAGE_LOAD_RATE} bytes read,
 * and each updated image is published, so the image is shown as it is loaded.
 */
public class LoadImageWorker extends StreamWorker<LoadWorkerModel<UpdatableImage>, UpdatableImage, ProgressModel> {

    public static final int IMAGE_LOAD_RATE = 3 * MEGA_INT;

    private final BinaryStreamer streamerToLoad;

    private final ImageStreamerView imageStreamerView;

    public LoadImageWorker(final LoadImageActionModel actionModel) {
        super(actionModel,
            actionModel.getImageStreamerView().getLoadWorkerModel(),
            () -> new ImageLoader(actionModel.getStreamerToLoad()).stream(),
            actionModel.getImageStreamerView().getLoadingIndicator().getProgressModel());
        this.streamerToLoad = actionModel.getStreamerToLoad();
        this.imageStreamerView = actionModel.getImageStreamerView();
    }

    @Override
    public void preparePipelineOpen(final PipelineStrategy pipelineStrategy,
                                    final PipelineListener<UpdatableImage> pipelineListener) {
        streamerToLoad.init();
    }

    @Override
    public void beforePipelineOpen() {
        imageStreamerView.reset(streamerToLoad);
    }

    @Override
    public void afterPipelineClose(boolean pipelineFailed) {
        streamerToLoad.finalizeStreaming();
        progressModel.finishProgress();
    }
}
