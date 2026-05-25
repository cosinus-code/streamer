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

package org.cosinus.streamer.ui.view.tree;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.worker.WorkerModel;

import java.util.List;

import static java.awt.BorderLayout.CENTER;

public class TreeStreamerView extends StreamerView<Streamer> {

    public static final String TREE_VIEW_NAME = "tree";

    private TreeViewer treeViewer;

    public TreeStreamerView(final PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        treeViewer = (TreeViewer) viewer;
        treeViewer.initComponents();

        ScrollPane scroll = new ScrollPane();
        scroll.setViewportView(treeViewer);
        streamerViewMainPanel.add(scroll, CENTER);

        initKeyActionsHandler();
        initCancelHandler();
        initFocusHandling();
        initCutCopyPasteActions();
        initDragAndDropActions();
    }

    @Override
    protected Viewer<Streamer> createViewer() {
        return new TreeViewer();
    }

    @Override
    public String getName() {
        return TREE_VIEW_NAME;
    }

    @Override
    protected long getItemsCount() {
        return treeViewer.getItemsCount();
    }

    @Override
    public Streamer getCurrentItem() {
        return treeViewer.getCurrentNode().getStreamer();
    }

    @Override
    public List<Streamer> getSelectedItems() {
        return treeViewer.getSelectedItems();
    }

    @Override
    public LoadWorkerModel<Streamer> getLoadWorkerModel() {
        return treeViewer;
    }

    @Override
    public WorkerModel<Streamer<Streamer>> getDeleteWorkerModel() {
        return treeViewer.getDeleteWorkerModel();
    }

    @Override
    public WorkerModel<Streamer> getCopyWorkerModel() {
        return treeViewer.getCopyWorkerModel();
    }

    @Override
    protected boolean haveStatus() {
        return false;
    }
}
