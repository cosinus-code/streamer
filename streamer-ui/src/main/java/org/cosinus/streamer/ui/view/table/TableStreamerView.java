/*
 * Copyright 2020 Cosinus Software
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

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static java.awt.BorderLayout.CENTER;
import static java.util.Optional.ofNullable;

public abstract class TableStreamerView<T extends Streamable> extends StreamerView<T> {

    protected DataTable<T> table;

    private JScrollPane scroll;

    protected TableStreamerView(PanelLocation location, Streamer<T> parentStreamer) {
        super(location, parentStreamer);
    }

    @Override
    public void initComponents() {
        this.table = createDataTable();
        table.init(this);
        table.initComponents();

        scroll = new JScrollPane();
        scroll.setEnabled(false);
        scroll.setViewportView(table);
        scroll.setFocusable(false);
        ofNullable(table.getBackground())
            .map(Color::getRGB)
            .map(Color::new)
            .ifPresent(scroll.getViewport()::setBackground);
        scroll.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                table.requestFocus();
            }
        });
        streamerViewMainPanel.add(scroll, CENTER);

        addComponentListener(new ResizeListener());

        super.initComponents();

        validateInContainer(table);
    }

    protected abstract DataTable createDataTable();

    @Override
    public LoadWorkerModel<T> getLoadWorkerModel() {
        return getDataTableModel();
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            table.setCurrentIndex(table.getCurrentIndex(), true);
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
    public String getCurrentItemIdentifier() {
        return table.getTableModel().getCurrentItemIdentifier();
    }

    @Override
    public String getNextItemIdentifier() {
        return table.getTableModel().getNextItemIdentifier();
    }

    @Override
    public Rectangle getCurrentRectangle() {
        int index = table.getCurrentIndex();
        int row = table.getTableModel().getRowForIndex(index);
        int col = table.getTableModel().getColumnForIndex(index);
        Rectangle rect = table.getCellRect(row, col, true);
        return new Rectangle(rect.x + 21, rect.y, rect.width - 21, rect.height + 2);
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        table.requestFocus();
    }

    @Override
    public void updateForm() {
        super.updateForm();
        table.updateForm();
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
    public void selectCurrentContent() {
        table.selectCurrentItem();
    }

    @Override
    public void workerStarted(LoadWorkerModel<T> loadWorkerModel) {
        super.workerStarted(loadWorkerModel);
        table.reset();
    }

    @Override
    public void workerUpdated(LoadWorkerModel<T> loadWorkerModel) {
        ofNullable(loadWorkerModel.getContentIdentifier())
            .ifPresent(this::findContent);
        getDataTableModel().fireTableDataChanged();

        super.workerUpdated(loadWorkerModel);
    }

    @Override
    public void workerFinished(LoadWorkerModel<T> loadWorkerModel) {
        super.workerFinished(loadWorkerModel);
        if (isActive()) {
            if (table.getCurrentIndex() < 0) {
                table.setCurrentIndex(0, true);
            } else if (table.getCurrentIndex() >= table.getItemsCount()) {
                table.setCurrentIndex(table.getItemsCount() - 1, true);
            }
        }
    }

    private DataTableModel<T> getDataTableModel() {
        return (DataTableModel<T>) table.getModel();
    }

    @Override
    public void translate() {
        super.translate();
        table.translate();
    }

    private class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            table.onResize(scroll.getWidth() - 30, scroll.getHeight() - 20);
        }
    }
}
