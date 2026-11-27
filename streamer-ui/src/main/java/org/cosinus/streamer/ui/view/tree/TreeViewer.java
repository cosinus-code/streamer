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
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthIcon;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
import static org.cosinus.streamer.ui.view.StreamerView.INACTIVE_COLOR_GRAY_FACTOR;
import static org.cosinus.swing.color.Colors.grayedColor;
import static org.cosinus.swing.color.SystemColor.TREE_SELECTION_BACKGROUND;
import static org.cosinus.swing.color.SystemColor.TREE_SELECTION_INACTIVE_BACKGROUND;

public class TreeViewer extends Tree implements Viewer<Streamer>, LoadWorkerModel<Streamer> {

    @Autowired
    private Preferences preferences;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    private StreamerHandler streamerHandler;

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Getter
    @Setter
    private StreamerTreeNode currentNode;

    private StreamerView<Streamer> streamerView;

    private DefaultTreeModel treeModel;

    private StreamerTreeNode treeRoot;

    private StreamerTreeNode loadingNode;

    /**
     * The renderer pane used to paint the current node of an inactive tree over its filled row
     */
    private final CellRendererPane inactiveCurrentRendererPane = new CellRendererPane();

    public TreeViewer() {
        super();
    }

    @Override
    public void initComponents() {
        add(inactiveCurrentRendererPane);
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
            repaint();
        }
    }

    /**
     * Check if a node is the current node of the tree when it became inactive.
     * <p>
     * The selection itself is cleared in an inactive tree, so that the actions on the inactive view
     * (like a drop) don't use it, but the current node is still shown with the inactive selection colors.
     *
     * @param node the node
     * @return true if the node is the current node when the tree became inactive
     */
    public boolean isInactiveCurrent(Object node) {
        return node != null && !hasFocus() && node == currentNode;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (paintsWholeSelectedRows()) {
            super.paintComponent(graphics);
        } else {
            paintWithWholeSelectedRows(graphics);
        }

        if (!hasFocus()) {
            paintInactiveCurrentNode(graphics);
        }
    }

    /**
     * @return true if the look and feel paints the whole rows of the selected nodes, like the synth ones (GTK, Nimbus)
     */
    protected boolean paintsWholeSelectedRows() {
        return getUI() instanceof SynthUI;
    }

    /**
     * Paint the tree, filling first the whole rows of the selected nodes with the selection color,
     * for the look and feels (like Metal or Aqua) where the renderer fills only the node text.
     */
    protected void paintWithWholeSelectedRows(Graphics graphics) {
        int[] selectionRows = getSelectionRows();
        if (ui == null || selectionRows == null || selectionRows.length == 0) {
            super.paintComponent(graphics);
            return;
        }

        Graphics2D graphics2D = (Graphics2D) graphics.create();
        try {
            if (isOpaque()) {
                graphics2D.setColor(getBackground());
                graphics2D.fillRect(0, 0, getWidth(), getHeight());
            }
            graphics2D.setColor(getSelectionBackground());
            for (int row : selectionRows) {
                ofNullable(getRowBounds(row))
                    .ifPresent(bounds -> graphics2D.fillRect(0, bounds.y, getWidth(), bounds.height));
            }

            ui.paint(graphics2D, this);
        } finally {
            graphics2D.dispose();
        }
    }

    /**
     * @return the selection color of the renderer, or the look and feel one
     */
    private Color getSelectionBackground() {
        return getCellRenderer() instanceof DefaultTreeCellRenderer renderer &&
            renderer.getBackgroundSelectionColor() != null ?
            renderer.getBackgroundSelectionColor() :
            uiHandler.getColor(TREE_SELECTION_BACKGROUND, getForeground());
    }

    protected void paintInactiveCurrentNode(Graphics graphics) {
        TreePath inactiveCurrentPath = ofNullable(currentNode)
            .map(StreamerTreeNode::getPath)
            .map(TreePath::new)
            .orElse(null);
        Rectangle bounds = ofNullable(inactiveCurrentPath)
            .map(this::getPathBounds)
            .orElse(null);
        if (bounds == null) {
            return;
        }

        Graphics2D graphics2D = (Graphics2D) graphics.create();
        try {
            graphics2D.setColor(uiHandler.getColor(
                TREE_SELECTION_INACTIVE_BACKGROUND,
                grayedColor(getBackground(), INACTIVE_COLOR_GRAY_FACTOR)));
            graphics2D.fillRect(0, bounds.y, getWidth(), bounds.height);

            int row = getRowForPath(inactiveCurrentPath);
            boolean expanded = isExpanded(inactiveCurrentPath);
            boolean leaf = getModel().isLeaf(currentNode);
            if (!leaf) {
                paintExpandIcon(graphics2D, bounds, expanded);
            }
            Component rendererComponent = getCellRenderer()
                .getTreeCellRendererComponent(this, currentNode, false, expanded, leaf, row, false);
            inactiveCurrentRendererPane.paintComponent(graphics2D, rendererComponent, this,
                bounds.x, bounds.y, bounds.width, bounds.height, true);
        } finally {
            graphics2D.dispose();
        }
    }

    /**
     * Paint the expand icon of a row the same way the look and feel does, centered before the node.
     */
    private void paintExpandIcon(Graphics graphics, Rectangle bounds, boolean expanded) {
        if (getUI() instanceof BasicTreeUI treeUI) {
            Icon icon = expanded ? treeUI.getExpandedIcon() : treeUI.getCollapsedIcon();
            if (icon != null) {
                boolean leftToRight = getComponentOrientation().isLeftToRight();
                int middleX = leftToRight ?
                    bounds.x - treeUI.getRightChildIndent() + 1 :
                    bounds.x + bounds.width + treeUI.getRightChildIndent() - 1;
                int middleY = bounds.y + bounds.height / 2;
                if (icon instanceof SynthIcon synthIcon) {
                    // the synth icons (like the GTK ones) need a context to be painted
                    SynthContext context = new SynthContext(this, Region.TREE,
                        SynthLookAndFeel.getStyle(this, Region.TREE), SynthConstants.ENABLED);
                    int width = synthIcon.getIconWidth(context);
                    int height = synthIcon.getIconHeight(context);
                    synthIcon.paintIcon(context, graphics,
                        middleX - (leftToRight ? (int) Math.ceil(width / 2.0) : width / 2),
                        middleY - height / 2, width, height);
                } else {
                    icon.paintIcon(this, graphics,
                        middleX - icon.getIconWidth() / 2,
                        middleY - icon.getIconHeight() / 2);
                }
            }
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
