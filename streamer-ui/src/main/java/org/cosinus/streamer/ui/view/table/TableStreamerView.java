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

package org.cosinus.streamer.ui.view.table;

import org.cosinus.stream.swing.ExtendedContainer;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.DoHereModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.menu.MenuHandler;
import org.cosinus.streamer.ui.view.DefaultStreamerView;
import org.cosinus.streamer.ui.view.FindPanel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.worker.WorkerListener;
import org.cosinus.swing.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Stream;

import static java.awt.BorderLayout.CENTER;
import static java.lang.Math.max;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.CopyHereAction.COPY_HERE_ACTION_ID;
import static org.cosinus.streamer.ui.action.CreateStreamerAction.CREATE_STREAMER_ACTION_ID;
import static org.cosinus.streamer.ui.action.LinkHereAction.LINK_HERE_ACTION_ID;
import static org.cosinus.streamer.ui.action.MoveHereAction.MOVE_HERE_ACTION_ID;
import static org.cosinus.streamer.ui.action.PackHereAction.PACK_HERE_ACTION_ID;
import static org.cosinus.streamer.ui.menu.MenuHandler.SEPARATOR;
import static org.cosinus.swing.action.ActionController.*;

public abstract class TableStreamerView<T extends Streamable>
    extends DefaultStreamerView<T> implements ExtendedContainer {

    public static final String STATUS_ITEMS_COUNT_KEY = "status-items-count";

    public static final String STATUS_SELECTED_ITEMS_COUNT_KEY = "status-selected-items-count";

    protected DataTable<T> table;

    private ScrollPane scroll;

    protected PopupMenu popupContextMenu;

    protected TableStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        this.table = createDataTable();
        table.init(this);
        table.initComponents();

        scroll = new ScrollPane();
        scroll.setEnabled(false);
        scroll.setViewportView(table);
        scroll.setFocusable(false);
        scroll.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                table.requestFocus();
            }
        });

        streamerViewMainPanel.add(scroll, CENTER);

        addComponentListener(new ResizeListener());

        popupContextMenu = menuHandler.createPopupMenu(this,
            CUT_ACTION_ID,
            COPY_ACTION_ID,
            PASTE_ACTION_ID,
            SEPARATOR,
            CREATE_STREAMER_ACTION_ID);
        menuHandler.addContextMenu(scroll, popupContextMenu);

        table.setDragEnabled(true);

        TransferHandler transferHandler = new TableTransferHandler<>(this);
        table.setTransferHandler(transferHandler);
        this.setTransferHandler(transferHandler);

        actionController.addCutCopyPasteActions(this);

        super.initComponents();
    }

    public void showDragAndDropPopup(final JComponent component,
                                     final StreamerView<?> sourceView,
                                     final Point dropPoint,
                                     final List<String> paths) {
        DoHereModel model = DoHereModel
            .builder()
            .paths(paths)
            .sourceView(sourceView)
            .destinationView(this)
            .useSelectedItemAsDestination(true)
            .build();

        PopupMenu popupDragAndDrop = new PopupMenu();
        popupDragAndDrop.add(menuHandler.createMenuItem(this, COPY_HERE_ACTION_ID, model));
        popupDragAndDrop.add(menuHandler.createMenuItem(this, MOVE_HERE_ACTION_ID, model));
        popupDragAndDrop.add(menuHandler.createMenuItem(this, PACK_HERE_ACTION_ID, model));
        popupDragAndDrop.add(menuHandler.createMenuItem(this, LINK_HERE_ACTION_ID, model));
        popupDragAndDrop.show(component, dropPoint.x, dropPoint.y);
    }

    @Override
    protected Container getContainer() {
        return table;
    }

    protected abstract DataTable<T> createDataTable();

    @Override
    public LoadWorkerModel<T> getLoadWorkerModel() {
        return getDataTableModel();
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            table.setCurrentIndex(max(table.getCurrentIndex(), 0), true);
        } else {
            table.getSelectionModel().clearSelection();
        }
        super.setActive(active);
    }

    @Override
    public T getCurrentItem() {
        return table.getCurrentItem();
    }

    @Override
    public List<T> getSelectedItems() {
        return table.getSelectedItems();
    }

    @Override
    public List<ViewItem> getAllViewItems() {
        return table.getAllViewItems();
    }

    @Override
    public String getCurrentItemIdentifier() {
        return table.getTableModel().getCurrentItemIdentifier();
    }

    @Override
    public String getNextItemIdentifier() {
        return table.getTableModel().getNextItemIdentifier();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        table.requestFocus();
    }

    @Override
    public void updateForm() {
        super.updateForm();

        ofNullable(table.getBackground())
            .map(Color::getRGB)
            .map(Color::new)
            .ifPresent(scroll.getViewport()::setBackground);
        table.updateForm();
    }

    @Override
    public Stream<Component> streamAdditionalContainers() {
        return Stream.of(alternativeViewsPopup, popupContextMenu);
    }

    @Override
    public void findContent(String name) {
        table.findViewItem(name);
    }

    @Override
    public void goHome() {
        table.setCurrentIndex(0);
    }

    @Override
    public void goEnd() {
        table.setCurrentIndex(table.getItemsCount() - 1);
    }

    @Override
    public void goNext() {
        table.goNext();
    }

    @Override
    public void addCurrentItemToSelectionAndGoNext() {
        table.addCurrentItemToSelectionAndGoNext();
    }

    @Override
    public <V> WorkerListener<LoadWorkerModel<V>, V> getLoadWorkerListener() {
        return new TableStreamerViewLoadWorkerListener<>(this);
    }

    @Override
    public void fireContentChanged() {
        getDataTableModel().fireContentChanged();
        super.fireContentChanged();
    }

    @Override
    public String getStatus() {
        int selectedItemsCount = table.getSelectedItems().size();
        return selectedItemsCount > 0 ?
            translator.translate(
                STATUS_SELECTED_ITEMS_COUNT_KEY, selectedItemsCount, getDataTableModel().getLoadedSize()) :
            translator.translate(
                STATUS_ITEMS_COUNT_KEY, getDataTableModel().getLoadedSize());
    }

    public DataTableModel<T> getDataTableModel() {
        return (DataTableModel<T>) table.getModel();
    }

    @Override
    public void reset(final Streamer<T> parentStreamer) {
        super.reset(parentStreamer);
        table.reset(parentStreamer);
    }

    @Override
    public void translate() {
        super.translate();
        table.translate();
        popupContextMenu.translate();
        updateStatus();
    }

    private class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            table.onResize(scroll.getWidth() - 30, scroll.getHeight() - 20);
        }
    }

    @Override
    protected FindPanel createFindTextPanel() {
        return new FindStreamerPanel<>(this);
    }

    @Override
    public WorkerModel<Streamer<T>> getDeleteWorkerModel() {
        return getDataTableModel().getDeleteWorkerModel();
    }

    @Override
    public WorkerModel<T> getCopyWorkerModel() {
        return getDataTableModel().getCopyWorkerModel();
    }

//    public class DragMouseAdapter extends MouseAdapter {
//        public void mousePressed(MouseEvent event) {
//            int index = getClosestIndex(event.getPoint());
//            if(index > -1) setCurrentIndex(index);
//            requestFocus();
//
//            clearSelection();
//            pointStart = event.getPoint();
//        }
//
//        public void mouseReleased(MouseEvent e) {
//            pointStart = null;
//            pointEnd = null;
//            repaint();
//        }
//    }
//
//    public class DragMouseMotionAdapter extends MouseMotionAdapter {
//        public void mouseDragged(MouseEvent event) {
//            pointEnd = event.getPoint();
//            List<Integer> indexes = getIndexesInRect(pointStart, pointEnd);
//            selectElements(indexes, true, null, false);
//            repaint();
//        }
//    }
}
