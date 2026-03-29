/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.ui.view.binary;

import lombok.Getter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.form.BlockCaret;
import org.cosinus.swing.form.ScrollPane;

import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static java.awt.BorderLayout.CENTER;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class BinaryStreamerView extends StreamerView<byte[]> {

    public static final String BINARY_VIEWER = "binary-viewer";

    public static final String STATUS_BINARY_SIZE = "status-binary-size";

    @Getter
    private BinaryEditor binaryEditor;

    private ScrollPane scroll;

    public BinaryStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        binaryEditor = new BinaryEditor(this);
        binaryEditor.initComponents();

        scroll = new ScrollPane();
        scroll.setViewportView(binaryEditor);
        scroll.setEnabled(false);
        scroll.setFocusable(false);
        scroll.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                binaryEditor.getBinaryHexaView().requestFocus();
            }
        });

        ofNullable(binaryEditor.getBackground())
            .map(Color::getRGB)
            .map(Color::new)
            .ifPresent(scroll.getViewport()::setBackground);

        streamerViewMainPanel.add(scroll, CENTER);

        addComponentListener(new ResizeListener());
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        binaryEditor.getBinaryHexaView().requestFocus();
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            binaryEditor.getBinaryHexaView().setCaret(new BlockCaret(binaryEditor.getBinaryHexaView()));
            int binaryPosition = binaryEditor.byteIndexToBinaryPosition(binaryEditor.getCurrentByteIndex());
            binaryEditor.getBinaryHexaView().setCaretPosition(binaryPosition);
        } else {
            binaryEditor.getBinaryHexaView().setCaret(new DefaultCaret());
        }
        super.setActive(active);
    }

    private class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            binaryEditor.onResize(scroll.getWidth(), scroll.getHeight());
        }
    }

    @Override
    public String getStatus() {
        return translator.translate(STATUS_BINARY_SIZE, getParentStreamer().getSize());
    }

    @Override
    public String getName() {
        return BINARY_VIEWER;
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
    public BinaryEditor getLoadWorkerModel() {
        return binaryEditor;
    }

    @Override
    public BinaryLoadListener getLoadWorkerListener() {
        return new BinaryLoadListener(this);
    }

    @Override
    protected Container getContainer() {
        return binaryEditor;
    }
}
