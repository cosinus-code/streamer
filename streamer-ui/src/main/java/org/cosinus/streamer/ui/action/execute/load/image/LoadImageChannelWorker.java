/*
 * Copyright 2026 Cosinus Software
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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.worker.ChannelWorker;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.swing.error.ActionException;
import org.cosinus.swing.image.UpdatableImage;

import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;

import static org.cosinus.streamer.ui.action.execute.load.image.LoadImageWorker.IMAGE_LOAD_RATE;

/**
 * Alternative to {@link LoadImageWorker}, which reads the image through the {@link FileChannel} of the streamer,
 * instead of its input stream, when the streamer supports it (see {@link BinaryStreamer#supportsChannel()}).
 * <p>
 * The image is read in chunks of {@link LoadImageWorker#IMAGE_LOAD_RATE} bytes,
 * and it is updated and published after each chunk, as it is loaded.
 */
public class LoadImageChannelWorker extends ChannelWorker<LoadWorkerModel<UpdatableImage>, UpdatableImage> {

    private final ImageStreamerView imageStreamerView;

    private ImageLoader imageLoader;

    /**
     * The number of bytes read so far.
     */
    private long loadedSize;

    public LoadImageChannelWorker(final LoadImageActionModel actionModel) {
        super(actionModel,
            actionModel.getImageStreamerView().getLoadWorkerModel(),
            actionModel.getImageStreamerView().getLoadingIndicator().getProgressModel(),
            actionModel.getStreamerToLoad());
        this.imageStreamerView = actionModel.getImageStreamerView();
    }

    @Override
    protected void doWork() {
        imageStreamerView.reset(binaryStreamer);
        binaryStreamer.init();
        imageLoader = new ImageLoader(binaryStreamer);
        loadedSize = 0;
        progressModel.startProgress(binaryStreamer.getSize());
        try {
            super.doWork();
        } catch (UncheckedIOException ex) {
            throw new ActionException(ex,
                "action-error", translator.translate(actionModel.getActionId()), ex.getMessage());
        } finally {
            imageLoader = null;
            binaryStreamer.finalizeStreaming();
            progressModel.finishProgress();
        }
    }

    /**
     * The progress is updated with the bytes read, before the image is decoded from them,
     * as the image is not decoded after each chunk read.
     */
    @Override
    protected void consume(byte[] bytes) {
        loadedSize += bytes.length;
        updateProgress(progress -> progress.updateProgress(loadedSize, binaryStreamer.getSize()));
        UpdatableImage image = imageLoader.updateAndGet(bytes);
        checkStatusAndPublish(image);
    }

    @Override
    protected long offset() {
        return 0;
    }

    /**
     * @return no limit, the whole image is read
     */
    @Override
    protected long limit() {
        return Long.MAX_VALUE;
    }

    @Override
    protected int chunkSize() {
        return IMAGE_LOAD_RATE;
    }
}
