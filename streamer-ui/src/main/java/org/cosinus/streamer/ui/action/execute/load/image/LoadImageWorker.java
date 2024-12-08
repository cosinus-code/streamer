package org.cosinus.streamer.ui.action.execute.load.image;

import error.ActionException;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.stream.pipeline.PipelineListener;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipeline;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryPipelineStrategy;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryStreamConsumer;
import org.cosinus.streamer.api.worker.Worker;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.image.ImageStreamerView;
import org.cosinus.swing.image.UpdatableImage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import static org.cosinus.swing.format.FormatHandler.MEGA;

public class LoadImageWorker extends Worker<LoadWorkerModel<byte[], UpdatableImage>, UpdatableImage>
    implements BinaryPipeline, PipelineListener<byte[]>, BinaryPipelineStrategy {

    public static final int IMAGE_LOAD_RATE = 3 * MEGA;

    private final BinaryStreamer streamerToLoad;

    private final ImageStreamerView imageStreamerView;

    public LoadImageWorker(final LoadImageActionModel actionModel) {
        super(actionModel, actionModel.getImageStreamerView().getLoadWorkerModel());
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

    public void publishImage(final UpdatableImage image) {
        checkWorkerStatus();
        publish(image);
    }

    @Override
    public int getPipelineRate() {
        return IMAGE_LOAD_RATE;
    }
}
