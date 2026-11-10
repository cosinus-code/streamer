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

package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.menu.MenuHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.ImageSettings;
import org.cosinus.swing.image.UpdatableImage;
import org.cosinus.swing.menu.Menu;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.menu.RadioButtonMenuItem;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static java.awt.Color.black;
import static java.awt.Color.gray;
import static java.awt.event.KeyEvent.*;
import static java.awt.event.MouseEvent.BUTTON3;
import static java.lang.Math.clamp;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.streamer.ui.action.DeleteStreamerAction.DELETE_STREAMER_ACTION_ID;
import static org.cosinus.streamer.ui.action.ExecuteStreamerAction.EXECUTE_STREAMER_ACTION_ID;
import static org.cosinus.streamer.ui.action.ExecuteStreamerWithAction.EXECUTE_STREAMER_WITH_ACTION_ID;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;
import static org.cosinus.streamer.ui.action.MoveToTrashStreamerAction.MOVE_TO_TRASH_STREAMER_ACTION_NAME;
import static org.cosinus.streamer.ui.menu.MenuHandler.SEPARATOR;
import static org.cosinus.swing.image.ImageSettings.*;

public class ImageViewer extends Panel implements Viewer<byte[]>, LoadWorkerModel<UpdatableImage>, ActionListener {

    public static final String IMAGE_SETTINGS_SPEED = "image-settings-speed";
    public static final String IMAGE_SETTINGS_QUALITY = "image-settings-quality";
    public static final String IMAGE_SETTINGS_BALANCED = "image-settings-balanced";

    public static final String IMAGE_QUALITY = "image-quality";

    /**
     * The factor the scale is multiplied (zoom in) or divided (zoom out) by.
     */
    public static final double ZOOM_STEP = 1.25;

    public static final double MIN_ZOOM = 0.05;

    public static final double MAX_ZOOM = 16;

    @Autowired
    private ActionController actionController;

    @Autowired
    private transient ClasspathResourceResolver resourceResolver;

    @Autowired
    private transient ImageHandler imageHandler;

    @Autowired
    private MenuHandler menuHandler;

    private ImageStreamerView streamerView;

    private Map<String, ImageSettings> imageSettingsMap;

    private PopupMenu popupContextMenu;

    private Streamer<byte[]> binaryStreamer;

    private UpdatableImage originalImage;

    private ImageSettings imageSettings = QUALITY;

    private boolean loaded;

    private boolean active = true;

    /**
     * The scale the image is shown at, or null to fit the image in the viewer (shrunk, never enlarged).
     */
    private Double zoom;

    @Override
    public void initComponents() {
        super.initComponents();

        imageSettingsMap = new LinkedHashMap<>();
        imageSettingsMap.put(IMAGE_SETTINGS_SPEED, SPEED);
        imageSettingsMap.put(IMAGE_SETTINGS_QUALITY, QUALITY);
        imageSettingsMap.put(IMAGE_SETTINGS_BALANCED, SPEED_QUALITY_BALANCE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ofNullable(originalImage)
            .map(UpdatableImage::getImage)
            .ifPresentOrElse(
                image -> drawImage(g, image),
                () -> drawBrokenImage(g));
        if (active) {
            g.setColor(getInactiveColor());
            g.drawRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent keyEvent) {
        super.processComponentKeyEvent(keyEvent);
        if (keyEvent.getID() == KEY_PRESSED) {
            if (keyEvent.getKeyCode() == VK_RIGHT) {
                streamerView.showNextImage();
            } else if (keyEvent.getKeyCode() == VK_LEFT) {
                streamerView.showPreviousImage();
            } else if (keyEvent.getKeyCode() == VK_HOME) {
                streamerView.showFirstImage();
            } else if (keyEvent.getKeyCode() == VK_END) {
                streamerView.showLastImage();
            } else if (keyEvent.getKeyCode() == VK_DELETE) {
                streamerView.deleteCurrentImage();
            } else if (keyEvent.getKeyCode() == VK_ESCAPE) {
                actionController.runAction(GO_TO_PARENT_ACTION);
            } else {
                actionController.runActionByKeyStroke(keyEvent);
            }
        }
    }

    /**
     * Reset the viewer to show a new image: the current image is cleared before the new one is loaded,
     * so the background is shown while loading, and the broken photo icon if no image is loaded.
     */
    @Override
    public void reset(final Streamer<byte[]> binaryStreamer) {
        this.binaryStreamer = binaryStreamer;
        originalImage = null;
        loaded = false;
        zoom = null;
        setPreferredSize(null);
        revalidate();
        repaint();
    }

    /**
     * Enlarge the image, starting from the scale currently shown.
     */
    public void zoomIn() {
        getShownImage()
            .ifPresent(image -> setZoom(image, getCurrentScale(image) * ZOOM_STEP));
    }

    /**
     * Shrink the image, starting from the scale currently shown.
     */
    public void zoomOut() {
        getShownImage()
            .ifPresent(image -> setZoom(image, getCurrentScale(image) / ZOOM_STEP));
    }

    /**
     * Show the image fitted in the viewer again.
     */
    public void zoomReset() {
        getShownImage()
            .ifPresent(image -> setZoom(image, null));
    }

    /**
     * @return the current zoom, or null if the image is fitted in the viewer
     */
    public Double getZoom() {
        return zoom;
    }

    private Optional<BufferedImage> getShownImage() {
        return ofNullable(originalImage)
            .map(UpdatableImage::getImage);
    }

    /**
     * Set the zoom, keeping the point at the center of the visible area at its center.
     * The preferred size of the viewer is the size of the zoomed image, so it is scrolled when larger than the view.
     */
    private void setZoom(BufferedImage image, Double newZoom) {
        Rectangle visible = getVisibleRect();
        double centerX = getWidth() > 0 ? (visible.getCenterX() / getWidth()) : 0.5;
        double centerY = getHeight() > 0 ? (visible.getCenterY() / getHeight()) : 0.5;

        zoom = ofNullable(newZoom)
            .map(value -> clamp(value, MIN_ZOOM, MAX_ZOOM))
            .orElse(null);
        setPreferredSize(ofNullable(zoom)
            .map(scale -> getScaledSize(image, scale))
            .orElse(null));
        revalidate();
        repaint();

        if (zoom != null) {
            invokeLater(() -> {
                Rectangle newVisible = getVisibleRect();
                int x = (int) (centerX * getWidth() - newVisible.width / 2d);
                int y = (int) (centerY * getHeight() - newVisible.height / 2d);
                scrollRectToVisible(new Rectangle(x, y, newVisible.width, newVisible.height));
            });
        }
    }

    /**
     * @return the scale the image is shown at: the zoom, or the scale fitting the image in the viewer
     */
    private double getCurrentScale(BufferedImage image) {
        return ofNullable(zoom)
            .orElseGet(() -> getWidth() > 0 && getHeight() > 0 ?
                min(1, min(
                    (double) getWidth() / image.getWidth(),
                    (double) getHeight() / image.getHeight())) :
                1);
    }

    private Dimension getScaledSize(BufferedImage image, double scale) {
        return new Dimension(
            max(1, (int) Math.round(image.getWidth() * scale)),
            max(1, (int) Math.round(image.getHeight() * scale)));
    }

    public Streamer<byte[]> getParentStreamer() {
        return binaryStreamer;
    }

    private void drawImage(Graphics g, BufferedImage image) {
        drawImageBackground(g);
//        if (imageSettings.isHighQualityOnScaling() &&
//            !imageHandler.isImageFitInCanvas(image, this)) {
//            image = imageHandler.fitImageToCanvas(image, this, imageSettings);
//        }
        if (zoom == null) {
            imageHandler.drawFitImage((Graphics2D) g, image,
                getWidth(), getHeight(), true, imageSettings, this);
        } else {
            Dimension size = getScaledSize(image, zoom);
            int x = max(0, (getWidth() - size.width) / 2);
            int y = max(0, (getHeight() - size.height) / 2);
            imageHandler.drawImage((Graphics2D) g, image, x, y, size.width, size.height, imageSettings, this);
        }
    }

    private void drawBrokenImage(Graphics g) {
        if (loaded) {
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
        g.setColor(active ? getBackground() : getInactiveColor());
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public long getProgressDone() {
        return ofNullable(originalImage)
            .map(UpdatableImage::getSize)
            .orElse(-1L);
    }

    @Override
    public long getTotalProgressSize() {
        return ofNullable(binaryStreamer)
            .map(Streamer::getSize)
            .orElse(-1L);
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

    @Override
    public void finishLoading(LoadWorkerModel<?> loadWorkerModel) {
        loaded = true;
        repaint();
    }

    public void setImageSettings(final ImageSettings imageSettings) {
        this.imageSettings = imageSettings;
        repaint();
    }

    public void initContextMenu() {
        popupContextMenu = menuHandler.createPopupMenu(this,
            EXECUTE_STREAMER_ACTION_ID,
            EXECUTE_STREAMER_WITH_ACTION_ID,
            SEPARATOR,
            MOVE_TO_TRASH_STREAMER_ACTION_NAME,
            DELETE_STREAMER_ACTION_ID,
            SEPARATOR);

        Menu imageQualityMenu = new Menu(IMAGE_QUALITY);
        popupContextMenu.add(imageQualityMenu);
        menuHandler.addContextMenu(this, popupContextMenu);

        ButtonGroup popupImageSettingsGroup = new ButtonGroup();
        imageSettingsMap.entrySet()
            .stream()
            .map(entry ->
                new RadioButtonMenuItem(this, entry.getValue().equals(imageSettings), entry.getKey()))
            .forEach(menuItem -> {
                imageQualityMenu.add(menuItem);
                popupImageSettingsGroup.add(menuItem);
            });
        translate();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == BUTTON3) {
                    popupContextMenu.show(ImageViewer.this,
                        mouseEvent.getX(),
                        mouseEvent.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                streamerView.requestFocusInWindow();
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
        popupContextMenu.translate();
    }

    @Override
    public void setView(StreamerView<byte[]> streamerView) {
        this.streamerView = (ImageStreamerView) streamerView;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Color getBackground() {
        return black;
    }

    private Color getInactiveColor() {
        return gray;
    }
}
