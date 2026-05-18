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

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.form.Tree;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
import static org.cosinus.stream.Streams.stream;

public class TreeViewer extends Tree implements Viewer<Streamer>, LoadWorkerModel<Streamer> {

    @Autowired
    private StreamerHandler streamerHandler;

    @Getter
    @Setter
    private StreamerTreeNode currentNode;

    private StreamerView<Streamer> streamerView;

    private DefaultTreeModel treeModel;

    private StreamerTreeNode treeRoot;

    private StreamerTreeNode loadingNode;

    public TreeViewer() {
        super();
    }

    @Override
    public void initComponents() {
        setRootVisible(false);
        setCellRenderer(new TreeNodeRenderer());
        getSelectionModel().setSelectionMode(DISCONTIGUOUS_TREE_SELECTION);
        setExpandsSelectedPaths(true);

        this.treeRoot = new StreamerTreeNode(streamerHandler.getMetaStreamer(), true);
        this.treeModel = new DefaultTreeModel(treeRoot);
        setModel(treeModel);

        addTreeSelectionListener(treeSelectionEvent -> {
            ofNullable(getLastSelectedPathComponent())
                .filter(node -> StreamerTreeNode.class.isAssignableFrom(node.getClass()))
                .map(StreamerTreeNode.class::cast)
                .ifPresent(this::setCurrentNode);
        });

        addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                ofNullable(event.getPath())
                    .ifPresent(TreeViewer.this::collapseChildren);
            }

            @Override
            public void treeWillExpand(TreeExpansionEvent event) {

            }
        });

        addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                StreamerTreeNode node = (StreamerTreeNode) event.getPath().getLastPathComponent();
                ofNullable(node)
                    .filter(not(StreamerTreeNode::isLoaded))
                    .filter(not(StreamerTreeNode::isLoading))
                    .map(StreamerTreeNode::getStreamer)
                    .map(Streamer.class::cast)
                    .ifPresent(streamerView::loadStreamer);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                int row = getClosestRowForLocation(mouseEvent.getX(), mouseEvent.getY());
                if (row >= 0) {
                    Rectangle bounds = getRowBounds(row);
                    if (mouseEvent.getY() >= bounds.y && mouseEvent.getY() < bounds.y + bounds.height) {
                        setSelectionRow(row);
                    }
                }
            }
        });
    }

    public void collapseChildren(TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        stream(node.children())
            .map(parent::pathByAddingChild)
            .filter(this::isExpanded)
            .forEach(path -> {
                collapseChildren(path);
                collapsePath(path);
            });
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            ofNullable(currentNode)
                .or(() -> ofNullable(loadingNode))
                .map(StreamerTreeNode::getPath)
                .map(TreePath::new)
                .ifPresent(currentPath -> {
                    setSelectionPath(currentPath);
                    scrollPathToVisible(currentPath);
                });
            requestFocusInWindow();
        } else {
            getSelectionModel().clearSelection();
        }
    }

    protected long getItemsCount() {
        return getModel().getChildCount(getModel().getRoot());
    }

    public List<Streamer> getSelectedItems() {
        return ofNullable(getSelectionPaths())
            .stream()
            .flatMap(Arrays::stream)
            .map(TreePath::getLastPathComponent)
            .filter(item -> Streamer.class.isAssignableFrom(item.getClass()))
            .map(Streamer.class::cast)
            .toList();
    }

    @Override
    public void reset(Streamer<Streamer> parentStreamer) {
        loadTreeNode(treeRoot, parentStreamer);
        try {
            loadingNode.removeAllChildren();
        } catch (Exception e) {
            // Ignore, don't break if other thread already did it
        }
        loadingNode.setLoading(true);
        setCurrentNode(loadingNode);
    }

    protected void loadTreeNode(StreamerTreeNode node, Streamer<?> streamer) {
        if (node.getStreamer().getUrlPath().equals(streamer.getUrlPath())) {
            loadingNode = node;
        } else {
            if (!node.isLoaded()) {
                node.loadChildren();
            }
            node.childStream()
                .sorted(reverseOrder())
                .filter(child -> child.getStreamer().isAncestorFor(streamer))
                .findFirst()
                .ifPresentOrElse(
                    childNode -> loadTreeNode(childNode, streamer),
                    () -> loadingNode = node);
        }
    }

    @Override
    public void update(List<Streamer> items) {
        items.stream()
            .map(loadingNode::createLoadingNode)
            .forEach(loadingNode::add);
        loadingNode.sort();
        treeModel.reload(loadingNode);
        expandPath(new TreePath(loadingNode.getPath()));
    }

    @Override
    public void finishLoading(LoadWorkerModel<?> loadWorkerModel) {
        if (loadingNode != null) {
            loadingNode.setLoading(false);
            loadingNode.setLoaded(true);
        }
    }

    @Override
    public void setView(StreamerView<Streamer> streamerView) {
        this.streamerView = streamerView;
    }
}
