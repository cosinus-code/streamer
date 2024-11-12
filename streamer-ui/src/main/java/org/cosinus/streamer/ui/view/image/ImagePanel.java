package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.image.ImageHandler;
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

public class ImagePanel extends Panel implements LoadWorkerModel<byte[], UpdatableImage> {

    @Autowired
    private ClasspathResourceResolver resourceResolver;

    @Autowired
    private ImageHandler imageHandler;

    private Streamer<byte[]> binaryStreamer;

    private UpdatableImage updatableImage;

    boolean finished;

    public void reset(Streamer<byte[]> binaryStreamer) {
        this.binaryStreamer = binaryStreamer;
        if (binaryStreamer == null) {
            updatableImage = null;
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
        ofNullable(updatableImage)
            .map(UpdatableImage::getImage)
            .ifPresentOrElse(image -> drawImage(image, g), () -> drawBrokenImage(g));
    }

    private void drawImage(BufferedImage image, Graphics g) {
        if (image.getWidth(this) > getWidth() || image.getHeight(this) > getHeight()) {
            image = imageHandler.scaleImage(image, getWidth(), getHeight());
            updatableImage.setImage(image);
        }
        drawImageBackground(g);

        int x = (getWidth() - image.getWidth(this)) / 2;
        int y = (getHeight() - image.getHeight(this)) / 2;
        g.drawImage(image, x, y, this);
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
        return updatableImage.getSize();
    }

    public void setImage(UpdatableImage updatableImage) {
        this.updatableImage = updatableImage;
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
}
