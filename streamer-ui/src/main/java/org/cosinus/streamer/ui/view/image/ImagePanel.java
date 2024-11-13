package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.ImageSettings;
import org.cosinus.swing.image.UpdatableImage;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.awt.Color.black;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.ImageSettings.SPEED;

public class ImagePanel extends Panel implements LoadWorkerModel<byte[], UpdatableImage> {

    @Autowired
    private transient ClasspathResourceResolver resourceResolver;

    @Autowired
    private transient ImageHandler imageHandler;

    private transient Streamer<byte[]> binaryStreamer;

    private transient UpdatableImage originalImage;

    private ImageSettings imageSettings = SPEED;

    private boolean finished;

    public void reset(final Streamer<byte[]> binaryStreamer) {
        this.binaryStreamer = binaryStreamer;
        if (binaryStreamer == null) {
            originalImage = null;
        }
        finished = false;
    }

    @Override
    public Streamer<byte[]> getParentStreamer() {
        return binaryStreamer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ofNullable(originalImage)
            .map(UpdatableImage::getImage)
            .ifPresentOrElse(
                image -> drawImage(g, image),
                () -> drawBrokenImage(g));
    }

    private void drawImage(Graphics g, BufferedImage image) {
        drawImageBackground(g);
        if (imageSettings.isHighQualityOnScaling() &&
            !imageHandler.isImageFitInCanvas(image, this)) {
            image = imageHandler.fitImageToCanvas(image, this, imageSettings);
        }
        imageHandler.drawFitImage((Graphics2D) g, image,
            getWidth(), getHeight(), true, imageSettings, this);
    }

    private void drawBrokenImage(Graphics g) {
        if (finished) {
            getBrokenPhotoIcon()
                .ifPresent(image -> g.drawImage(image, 0, 0, this));
        } else if (binaryStreamer != null) {
            drawImageBackground(g);
        }
    }

    private void drawImageBackground(Graphics g) {
        g.setColor(black);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    @Override
    public long getLoadedSize() {
        return originalImage.getSize();
    }

    public void setImage(UpdatableImage updatableImage) {
        this.originalImage = updatableImage;
    }

    @Override
    public void update(List<UpdatableImage> images) {
        images.stream()
            .filter(Objects::nonNull)
            .reduce((first, second) -> second)
            .ifPresent(this::setImage);
    }

    private Optional<BufferedImage> getBrokenPhotoIcon() {
        return resourceResolver.resolveImageAsBytes("broken-photo-icon.png")
            .map(imageHandler::getImage);
    }

    public void finish() {
        finished = true;
        repaint();
    }

    public void setImageSettings(final ImageSettings imageSettings) {
        this.imageSettings = imageSettings;
        repaint();
    }
}
