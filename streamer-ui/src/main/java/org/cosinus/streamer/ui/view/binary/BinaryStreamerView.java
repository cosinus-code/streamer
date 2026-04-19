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
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.text.SaveTextWorkerModel;
import org.cosinus.swing.worker.WorkerListener;

import java.awt.*;
import java.util.List;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class BinaryStreamerView extends StreamerView<byte[]> {

    public static final String BINARY_VIEWER = "binary-viewer";

    public static final String STATUS_BINARY_SIZE = "status-binary-size";

    @Getter
    private BinaryHexaEditor binaryEditor;

    public BinaryStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        binaryEditor = new BinaryHexaEditor(this);
        binaryEditor.initComponents();

        streamerViewMainPanel.add(binaryEditor, CENTER);
        streamerViewMainPanel.add(binaryEditor.getScrollBar(), EAST);
    }

    @Override
    public void reset(Streamer<byte[]> parentStreamer) {
        super.reset(parentStreamer);
        binaryEditor.reset();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        if (binaryEditor != null) {
            binaryEditor.requestFocus();
        }
    }

    @Override
    public void setActive(boolean active) {
        binaryEditor.setHideCaret(!active);
        super.setActive(active);
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
    public BinaryHexaEditor getLoadWorkerModel() {
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

    @Override
    public boolean isDirty() {
        return super.isDirty() || ofNullable(binaryEditor)
            .map(BinaryHexaEditor::isDirty)
            .orElse(false);
    }
}

