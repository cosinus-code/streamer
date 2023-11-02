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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.StreamedContent;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.RenamingStreamerView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import static java.awt.BorderLayout.CENTER;
import static java.util.Optional.ofNullable;

public abstract class TableStreamerView extends RenamingStreamerView {

    protected DataTable table;

    private JScrollPane scroll;

    protected TableStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        this.table = createDataTable();
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
        panContent.add(scroll, CENTER);
        table.init(this);

        addComponentListener(new ResizeListener());

        super.initComponents();

        validateInContainer(table);
    }

    protected abstract DataTable createDataTable();

    @Override
    public void setActive(boolean active) {
        if (active) {
            table.setCurrentIndex(table.getCurrentIndex());
        } else {
            table.getSelectionModel().clearSelection();
        }
    }

    @Override
    public Streamer getCurrentContent() {
        return table.getCurrentStreamer();
    }

    @Override
    public List<Streamer> getSelectedContent() {
        return table.getSelectedStreamers();
    }

    @Override
    public Optional<String> getSelectedContentIdentifier() {
        return getSelectedContent()
            .stream()
            .findFirst()
            .map(Streamer::getName);
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
//        scroll.setBorder(uiHandler.isLookAndFeelGTK() ?
//                                 Borders.createEmptyBorder() :
//                                 Borders.borderButtonIn());
        table.updateForm();
    }

    @Override
    public void findContent(String name) {
        table.findViewItem(name);
    }

    @Override
    public Streamer getLoadedStreamer() {
        return table.getCurrentFolder();
    }

    @Override
    public void goHome() {
        table.setCurrentIndex(0);
    }

    @Override
    public void goEnd() {
        table.setCurrentIndex(table.getStreamersCount() - 1);
    }

    @Override
    public void selectCurrentContent() {
        table.selectCurrentStreamer();
    }

    @Override
    public void internalUpdateContent(StreamedContent<Streamer> content) {
        getDataTableModel().updateContent(content);
        table.reset();

        if(getDataTableModel().getCurrentIndex() >= 0)
        {
            System.out.println(getCurrentLocation() + " -> currentIndex: " + getDataTableModel().getCurrentIndex());
            ofNullable(content.getContentIdentifier())
                .ifPresent(this::findContent);
        }
    }

    private DataTableModel getDataTableModel() {
        return (DataTableModel) table.getModel();
    }

    @Override
    public void workerStarted() {
        super.workerStarted();
        table.setCurrentIndex(-1);
    }

    @Override
    public void workerFinished() {
        if (isActive() && table.getCurrentIndex() < 0) {
            table.setCurrentIndex(0);
        }
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
