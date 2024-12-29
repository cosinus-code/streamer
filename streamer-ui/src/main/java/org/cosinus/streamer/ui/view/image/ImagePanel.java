package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.ImageSettings;
import org.cosinus.swing.image.UpdatableImage;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.menu.RadioButtonMenuItem;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.awt.Color.black;
import static java.awt.event.MouseEvent.BUTTON3;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.ImageSettings.QUALITY;
import static org.cosinus.swing.image.ImageSettings.SPEED;
import static org.cosinus.swing.image.ImageSettings.SPEED_QUALITY_BALANCE;

public class ImagePanel extends Panel implements LoadWorkerModel<byte[], UpdatableImage>, ActionListener {

    public static final String IMAGE_SETTINGS_SPEED = "image-settings-speed";
    public static final String IMAGE_SETTINGS_QUALITY = "image-settings-quality";
    public static final String IMAGE_SETTINGS_BALANCED = "image-settings-balanced";

    @Autowired
    private transient ClasspathResourceResolver resourceResolver;

    @Autowired
    private transient ImageHandler imageHandler;

    private final transient Map<String, ImageSettings> imageSettingsMap;

    private PopupMenu popupImageSettings;

    private transient Streamer<byte[]> binaryStreamer;

    private transient UpdatableImage originalImage;

    private transient ImageSettings imageSettings = QUALITY;

    private boolean finished;

    public ImagePanel() {
        imageSettingsMap = new LinkedHashMap<>();
        imageSettingsMap.put(IMAGE_SETTINGS_SPEED, SPEED);
        imageSettingsMap.put(IMAGE_SETTINGS_QUALITY, QUALITY);
        imageSettingsMap.put(IMAGE_SETTINGS_BALANCED, SPEED_QUALITY_BALANCE);
    }

    public void reset(final Streamer<byte[]> binaryStreamer) {
        this.binaryStreamer = binaryStreamer;
        if (binaryStreamer == null) {
            originalImage = null;
        }
        finished = false;
    }

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
                .ifPresent(image -> {
                    int x = (getWidth() - image.getWidth()) / 2;
                    int y = (getHeight() - image.getHeight()) / 2;
                    g.drawImage(image, x, y, this);
                });
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

    public void initHeaderPopup() {
        popupImageSettings = new PopupMenu();
        ButtonGroup popupImageSettingsGroup = new ButtonGroup();
        imageSettingsMap.entrySet()
            .stream()
            .map(entry -> new RadioButtonMenuItem(this, entry.getValue().equals(imageSettings), entry.getKey()))
            .forEach(menuItem -> {
                popupImageSettings.add(menuItem);
                popupImageSettingsGroup.add(menuItem);
            });
        translate();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == BUTTON3) {
                    popupImageSettings.show(ImagePanel.this,
                        mouseEvent.getX(),
                        mouseEvent.getY());
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof RadioButtonMenuItem menuItem) {
            ofNullable(imageSettingsMap.get(menuItem.getActionKey()))
                .ifPresent(this::setImageSettings);
        }
    }

    @Override
    public void translate() {
        popupImageSettings.translate();
    }
}
