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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageActionModel;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageExecutor;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.image.UpdatableImage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.awt.BorderLayout.CENTER;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_END;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_HOME;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;

/**
 * Image streamer view
 */
public class ImageStreamerView extends StreamerView<byte[], UpdatableImage> {

    public static final String IMAGE_VIEWER = "image-viewer";

    public static final String STATUS_CURRENT_IMAGE_POSITION = "status-current-image-position";

    @Autowired
    private ActionController actionController;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    @Autowired
    private LoadImageExecutor loadImageExecutor;

    private ImagePanel imagePanel;

    private List<BinaryStreamer> imageStreamers;

    private int currentImagePosition;

    public ImageStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        imagePanel = new ImagePanel(this);
        imagePanel.initHeaderPopup();

        ScrollPane scroll = new ScrollPane();
        scroll.setViewportView(imagePanel);
        streamerViewMainPanel.add(scroll, CENTER);

        setFocusTraversalKeysEnabled(false);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    actionController.runAction(GO_TO_PARENT_ACTION);
                } else if (keyEvent.getKeyCode() == VK_RIGHT) {
                    showNextImage();
                } else if (keyEvent.getKeyCode() == VK_LEFT) {
                    showPreviousImage();
                } else if (keyEvent.getKeyCode() == VK_HOME) {
                    showFirstImage();
                } else if (keyEvent.getKeyCode() == VK_END) {
                    showLastImage();
                } else if (keyEvent.getKeyCode() == VK_DELETE) {
                    deleteCurrentImage();
                } else {
                    actionController.runActionByKeyStroke(keyEvent);
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                try {
                    streamerViewHandler.setCurrentLocation(getCurrentLocation());
                } catch (Exception ex) {
                    errorHandler.handleError(ImageStreamerView.this, ex);
                }
            }
        });
    }

    @Override
    public String getName() {
        return IMAGE_VIEWER;
    }

    @Override
    public byte[] getCurrentItem() {
        return new byte[0];
    }

    @Override
    public List<byte[]> getSelectedItems() {
        return emptyList();
    }

    @Override
    public String getCurrentItemIdentifier() {
        return ofNullable(parentStreamer)
            .map(Streamer::getName)
            .orElse(null);
    }

    @Override
    public String getNextItemIdentifier() {
        return "";
    }

    @Override
    public LoadWorkerModel<UpdatableImage> getLoadWorkerModel() {
        return imagePanel;
    }

    @Override
    protected Container getContainer() {
        return null;
    }

    @Override
    public void reset(Streamer<byte[]> binaryStreamer) {
        imagePanel.reset(binaryStreamer);
        super.reset(binaryStreamer);
        imageStreamers = binaryStreamer.getParent()
            .stream()
            .filter(Objects::nonNull)
            .filter(Streamer::isImage)
            .map(Streamer::binaryStreamer)
            .toList();
        currentImagePosition = imageStreamers.indexOf(binaryStreamer) + 1;
    }

    @Override
    public void workerUpdated(LoadWorkerModel<UpdatableImage> loadWorkerModel) {
        super.workerUpdated(loadWorkerModel);
        imagePanel.repaint();
    }

    @Override
    public void workerFinished(LoadWorkerModel<UpdatableImage> loadWorkerModel) {
        super.workerFinished(loadWorkerModel);
        imagePanel.finish();
    }

    @Override
    public String getStatus() {
        return translator.translate(STATUS_CURRENT_IMAGE_POSITION,
            currentImagePosition,
            imageStreamers.size());
    }

    private void showNextImage() {
        getNextSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    private void showPreviousImage() {
        getPreviousSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    private void showFirstImage() {
        getFirstSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    private void showLastImage() {
        getLastSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    private Optional<BinaryStreamer> getNextSibling() {
        return getSiblingImageInOrder(imagePanel.getParentStreamer(), true, true);
    }

    private Optional<BinaryStreamer> getPreviousSibling() {
        return getSiblingImageInOrder(imagePanel.getParentStreamer(), false, true);
    }

    private Optional<BinaryStreamer> getFirstSibling() {
        return getSiblingImageInOrder(imagePanel.getParentStreamer(), true, false);
    }

    private Optional<BinaryStreamer> getLastSibling() {
        return getSiblingImageInOrder(imagePanel.getParentStreamer(), false, false);
    }

    private Optional<BinaryStreamer> getSiblingImageInOrder(Streamer<byte[]> binaryStreamer, boolean ascending, boolean relative) {
        return imageStreamers
            .stream()
            .filter(streamer -> !relative || areStreamsOrdered(binaryStreamer, streamer, ascending))
            .reduce((current, next) -> areStreamsOrdered(current, next, ascending) ? current : next);
    }

    private boolean areStreamsOrdered(Streamer<byte[]> current, Streamer<byte[]> next, boolean ascending) {
        return current.getName().compareTo(next.getName()) * (ascending ? 1 : -1) < 0;
    }

    private void deleteCurrentImage() {
        ofNullable(imagePanel.getParentStreamer())
            .ifPresent(binaryStreamer -> {
                binaryStreamer.delete(true);
                getNextSibling()
                    .or(this::getLastSibling)
                    .map(streamer -> new LoadImageActionModel(streamer, this))
                    .ifPresentOrElse(loadImageExecutor::execute, () -> {
                        imagePanel.reset(null);
                        imagePanel.repaint();
                    });
            });
    }

    @Override
    public void translate() {
        imagePanel.translate();
    }
}
