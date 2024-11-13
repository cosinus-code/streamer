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

    @Autowired
    private ActionController actionController;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    @Autowired
    private LoadImageExecutor loadImageExecutor;

    private ImagePanel imagePanel;

    public ImageStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        imagePanel = new ImagePanel();
        imagePanel.initHeaderPopup();

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(imagePanel);
        streamerViewMainPanel.add(scroll, CENTER);

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
        return "";
    }

    @Override
    public String getNextItemIdentifier() {
        return "";
    }

    @Override
    public LoadWorkerModel<byte[], UpdatableImage> getLoadWorkerModel() {
        return imagePanel;
    }

    @Override
    protected Container getContainer() {
        return null;
    }

    @Override
    public void reset(Streamer<byte[]> parentStreamer) {
        imagePanel.reset(parentStreamer);
        super.reset(parentStreamer);
    }

    @Override
    public void workerUpdated(LoadWorkerModel<byte[], UpdatableImage> loadWorkerModel) {
        super.workerUpdated(loadWorkerModel);
        imagePanel.repaint();
    }

    @Override
    public void workerFinished(LoadWorkerModel<byte[], UpdatableImage> loadWorkerModel) {
        super.workerFinished(loadWorkerModel);
        imagePanel.finish();
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
        return binaryStreamer.getParent()
            .stream()
            .filter(Objects::nonNull)
            .filter(Streamer::isImage)
            .map(Streamer::binaryStreamer)
            .filter(streamer -> !relative || areStreamsOrdered(binaryStreamer, streamer, ascending))
            .reduce((current, next) -> areStreamsOrdered(current, next, ascending) ? current : next);
    }

    private boolean areStreamsOrdered(Streamer<byte[]> current, Streamer<byte[]> next, boolean ascending) {
        return current.getName().compareTo(next.getName()) * (ascending ? 1 : -1) < 0;
    }

    private void deleteCurrentImage() {
        ofNullable(imagePanel.getParentStreamer())
            .ifPresent(binaryStreamer -> {
                binaryStreamer.delete();
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
