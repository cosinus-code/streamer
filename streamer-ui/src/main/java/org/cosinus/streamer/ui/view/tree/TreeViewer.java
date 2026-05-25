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
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Tree;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.awt.event.MouseEvent.MOUSE_CLICKED;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.rangeClosed;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;
import static org.cosinus.stream.Streams.stream;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SHOW_HIDDEN;

public class TreeViewer extends Tree implements Viewer<Streamer>, LoadWorkerModel<Streamer> {

    @Autowired
    private Preferences preferences;

    @Autowired
    protected ErrorHandler errorHandler;

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

//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent mouseEvent) {
//                int row = getClosestRowForLocation(mouseEvent.getX(), mouseEvent.getY());
//                if (row >= 0) {
//                    Rectangle bounds = getRowBounds(row);
//                    if (mouseEvent.getY() >= bounds.y && mouseEvent.getY() < bounds.y + bounds.height) {
//                        setSelectionRow(row);
//                    }
//                }
//            }
//        });
    }

    @Override
    public void processMouseEvent(MouseEvent mouseEvent) {
        try {
            if (mouseEvent.getID() == MOUSE_CLICKED) {
                if (isLeftMouseButton(mouseEvent)) {
                    int currentRow = getSelectionModel().getLeadSelectionRow();
                    int row = getClosestRowForLocation(mouseEvent.getX(), mouseEvent.getY());
                    if (row >= 0 && currentRow != row) {
                        Rectangle bounds = getRowBounds(row);
                        if (mouseEvent.getY() >= bounds.y && mouseEvent.getY() < bounds.y + bounds.height) {
                            if (mouseEvent.isControlDown()) {
                                getSelectionModel().addSelectionPath(new TreePath(getPathForRow(row)));
                                mouseEvent.consume();
                            } else if (mouseEvent.isShiftDown()) {
                                int min = min(currentRow, row);
                                int max = max(currentRow, row);
                                TreePath[] paths = rangeClosed(min, max)
                                    .mapToObj(this::getPathForRow)
                                    .toArray(TreePath[]::new);
                                getSelectionModel().setSelectionPaths(paths);
                                mouseEvent.consume();
                            } else {
                                setSelectionRow(row);
                                mouseEvent.consume();
                            }
                        }
                    }
                }
            }
            super.processMouseEvent(mouseEvent);
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
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
            .filter(treePath -> StreamerTreeNode.class.isAssignableFrom(treePath.getClass()))
            .map(StreamerTreeNode.class::cast)
            .<Streamer>map(StreamerTreeNode::getStreamer)
            .toList();
    }

    @Override
    public void reset(Streamer<Streamer> parentStreamer) {
        loadingNode = loadTreeNode(treeRoot, parentStreamer);
        try {
            loadingNode.removeAllChildren();
        } catch (Exception e) {
            // Ignore, don't break if other thread already did it
        }
        loadingNode.setLoading(true);
        setCurrentNode(loadingNode);
    }

    protected StreamerTreeNode loadTreeNode(StreamerTreeNode node, Streamer<?> streamer) {
        if (node.getStreamer().getUrlPath().equals(streamer.getUrlPath())) {
            return node;
        }

        if (!node.isLoaded()) {
            node.loadChildren();
            treeModel.reload(node);
        }
        return node.childStream()
            .sorted(reverseOrder())
            .filter(child -> child.getStreamer().isAncestorFor(streamer))
            .findFirst()
            .map(childNode -> loadTreeNode(childNode, streamer))
            .orElse(node);
    }

    @Override
    public void update(List<Streamer> items) {
        items.stream()
            .filter(item -> preferences.booleanPreference(SHOW_HIDDEN) || !item.isHidden())
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

    public WorkerModel<Streamer<Streamer>> getDeleteWorkerModel() {
        return streamers -> streamers
            .forEach(streamer ->
                findNode(streamer).ifPresent(DefaultMutableTreeNode::removeFromParent));
    }

    public WorkerModel<Streamer> getCopyWorkerModel() {
        return streamers -> streamers
            .forEach(streamer ->
                findParentNode(streamer)
                    .ifPresent(parent -> parent.add(parent.createLoadingNode(streamer))));
    }

    protected Optional<StreamerTreeNode> findNode(Streamer streamer) {
        return findNode(treeRoot, streamer);
    }

    protected Optional<StreamerTreeNode> findNode(StreamerTreeNode node, Streamer streamer) {
        if (node.getStreamer().getUrlPath().equals(streamer.getUrlPath())) {
            return Optional.of(node);
        }

        return node.childStream()
            .sorted(reverseOrder())
            .filter(child -> child.getStreamer().isAncestorFor(streamer))
            .findFirst()
            .flatMap(childNode -> findNode(childNode, streamer));
    }

    protected Optional<StreamerTreeNode> findParentNode(Streamer streamer) {
        return findNode(treeRoot, streamer);
    }

    protected Optional<StreamerTreeNode> findParentNode(StreamerTreeNode node, Streamer streamer) {
        return node.childStream()
            .sorted(reverseOrder())
            .filter(DefaultMutableTreeNode::getAllowsChildren)
            .filter(child -> child.getStreamer().isAncestorFor(streamer))
            .findFirst()
            .flatMap(childNode -> findNode(childNode, streamer));
    }
}
