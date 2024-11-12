package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.Collections.emptyList;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;

/**
 * Image streamer view
 */
public class ImageStreamerView extends StreamerView<byte[], UpdatableImage>  {

    public static final String IMAGE_VIEWER = "image-viewer";

    @Autowired
    private ActionController actionController;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    private ImagePanel imagePanel;

    public ImageStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        imagePanel = new ImagePanel();

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(imagePanel);
        streamerViewMainPanel.add(scroll, CENTER);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    actionController.runAction(GO_TO_PARENT_ACTION);
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
}
