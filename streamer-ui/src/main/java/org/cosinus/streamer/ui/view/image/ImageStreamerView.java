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

import lombok.Getter;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.image.LoadImageActionModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.image.UpdatableImage;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.awt.BorderLayout.CENTER;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

/**
 * Image streamer view
 */
public class ImageStreamerView extends StreamerView<byte[]> {

    public static final String IMAGE_VIEWER = "image-viewer";

    public static final String STATUS_CURRENT_IMAGE_POSITION = "status-current-image-position";

    @Autowired
    private LoadActionExecutor loadImageExecutor;

    @Getter
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
        imagePanel.initContextMenu();

        ScrollPane scroll = new ScrollPane();
        scroll.setViewportView(imagePanel);
        streamerViewMainPanel.add(scroll, CENTER);

        initCancelHandler();
        initFocusHandling();
    }

    @Override
    public String getName() {
        return IMAGE_VIEWER;
    }

    @Override
    public byte[] getCurrentItem() {
        return null;
    }

    @Override
    public List<byte[]> getSelectedItems() {
        return emptyList();
    }

    @Override
    public LoadWorkerModel<UpdatableImage> getLoadWorkerModel() {
        return imagePanel;
    }

    @Override
    protected Container getContainer() {
        return imagePanel;
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
    public ImageLoadWorkerListener getLoadWorkerListener() {
        return new ImageLoadWorkerListener(this);
    }

    @Override
    public String getStatus() {
        return translator.translate(STATUS_CURRENT_IMAGE_POSITION,
            currentImagePosition,
            imageStreamers.size());
    }

    public void showNextImage() {
        getNextSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    public void showPreviousImage() {
        getPreviousSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    public void showFirstImage() {
        getFirstSibling()
            .map(nextStreamer -> new LoadImageActionModel(nextStreamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    public void showLastImage() {
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

    public void deleteCurrentImage() {
        ofNullable(imagePanel.getParentStreamer())
            .ifPresent(binaryStreamer -> {
                binaryStreamer.delete(true);
                imageStreamers.remove(binaryStreamer);
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
