package org.cosinus.streamer.ui.action.execute.load.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.stream.pipeline.binary.BinaryStreamConsumer;
import org.cosinus.swing.image.UpdatableImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageLoader extends BinaryStreamConsumer {

    private static final Logger LOG = LogManager.getLogger(ImageLoader.class);

    private final LoadImageWorker loadImageWorker;

    private final Streamer<byte[]> streamerToLoad;

    private final UpdatableImage image;

    public ImageLoader(final LoadImageWorker loadImageWorker) {
        super(new ByteArrayOutputStream());
        this.loadImageWorker = loadImageWorker;
        this.streamerToLoad = loadImageWorker.getStreamerToLoad();
        this.image = new UpdatableImage();
    }

    @Override
    public void accept(byte[] item) {
        super.accept(item);
        updateImage();
    }

    private byte[] getImageBytes() {
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    private void updateImage() {
        try {
            image.update(getImageBytes());
            loadImageWorker.publishImage(image);
        } catch (IOException ex) {
            LOG.warn("Failed to load image ({}): {}", ex.getMessage(), streamerToLoad.getPath());
        }
    }
}
