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
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.streamer.ui.view.ViewItem;
import org.cosinus.streamer.ui.view.ViewItemComparator;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.image.UpdatableImage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.awt.BorderLayout.CENTER;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Image streamer view
 */
public class ImageStreamerView extends StreamerView<byte[]> {

    public static final String IMAGE_VIEWER = "image-viewer";

    public static final String STATUS_CURRENT_IMAGE_POSITION = "status-current-image-position";

    @Autowired
    private LoadActionExecutor loadImageExecutor;

    @Getter
    private ImageViewer imageViewer;

    private List<BinaryStreamer> imageStreamers;

    private int currentImagePosition;

    protected ViewItemComparator sorting;

    public ImageStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        imageViewer = (ImageViewer) viewer;
        imageViewer.initContextMenu();

        ScrollPane scroll = new ScrollPane();
        scroll.setViewportView(imageViewer);
        streamerViewMainPanel.add(scroll, CENTER);

        initCancelHandler();
        initFocusHandling();

        sorting = new ViewItemComparator();
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
        return imageViewer;
    }

    @Override
    protected Viewer<byte[]> createViewer() {
        return new ImageViewer();
    }

    @Override
    protected long getItemsCount() {
        return imageStreamers.size();
    }

    /**
     * The images of the folder are listed again, in the current sorting order,
     * so the position and the number of images are up-to-date (e.g. after an image was deleted).
     */
    @Override
    public void reset(Streamer<byte[]> binaryStreamer) {
        imageViewer.reset(binaryStreamer);
        super.reset(binaryStreamer);
        imageStreamers = binaryStreamer.getParent()
            .stream()
            .filter(Objects::nonNull)
            .filter(Streamer::isImage)
            .map(ViewItem::new)
            .sorted(sorting)
            .map(ViewItem::getStreamable)
            .map(Streamer.class::cast)
            .map(Streamer::binaryStreamer)
            .collect(toCollection(ArrayList::new));
        currentImagePosition = imageStreamers.indexOf(binaryStreamer) + 1;

        invokeLater(this::updateStatus);
    }

    @Override
    public String getStatus() {
        return translator.translate(STATUS_CURRENT_IMAGE_POSITION,
            currentImagePosition,
            imageStreamers.size());
    }

    public void showNextImage() {
        showImage(getImage(currentImagePosition));
    }

    public void showPreviousImage() {
        showImage(getImage(currentImagePosition - 2));
    }

    public void showFirstImage() {
        showImage(getImage(0));
    }

    public void showLastImage() {
        showImage(getImage(imageStreamers.size() - 1));
    }

    private void showImage(Optional<BinaryStreamer> imageStreamer) {
        imageStreamer
            .map(streamer -> new LoadImageActionModel(streamer, this))
            .ifPresent(loadImageExecutor::execute);
    }

    private Optional<BinaryStreamer> getImage(int index) {
        return index >= 0 && index < imageStreamers.size() ?
            Optional.of(imageStreamers.get(index)) :
            Optional.empty();
    }

    /**
     * Delete the current image and show the next one, or the previous one if it was the last image.
     * If the image cannot be deleted, it is still shown.
     */
    public void deleteCurrentImage() {
        ofNullable(imageViewer.getParentStreamer())
            .filter(binaryStreamer -> binaryStreamer.delete(true))
            .ifPresent(binaryStreamer -> {
                int deletedIndex = currentImagePosition - 1;
                imageStreamers.remove(binaryStreamer);
                getImage(deletedIndex)
                    .or(() -> getImage(imageStreamers.size() - 1))
                    .map(streamer -> new LoadImageActionModel(streamer, this))
                    .ifPresentOrElse(loadImageExecutor::execute, () -> {
                        imageViewer.reset(null);
                        imageViewer.repaint();
                        currentImagePosition = 0;
                        updateStatus();
                    });
            });
    }

    @Override
    public void zoomIn() {
        imageViewer.zoomIn();
    }

    @Override
    public void zoomOut() {
        imageViewer.zoomOut();
    }

    @Override
    public void zoomReset() {
        imageViewer.zoomReset();
    }

    @Override
    public void translate() {
        imageViewer.translate();
    }

    @Override
    public ViewItemComparator getSorting() {
        return sorting;
    }

    @Override
    public void setSorting(ViewItemComparator sorting) {
        this.sorting = sorting;
    }
}
