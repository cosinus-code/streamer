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

package org.cosinus.streamer.ui.view.table.grid;

import lombok.Getter;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.streamer.ui.view.table.grid.header.GridHeader;
import org.cosinus.swing.menu.CheckBoxMenuItem;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ROW_HEIGHT;
import static org.cosinus.streamer.ui.view.table.grid.GridCellRenderer.CELL_ICON_SIZE;

public class GridTable<T extends Streamable> extends DataTable<T> implements ActionListener {

    @Autowired
    public ApplicationUIHandler uiHandler;

    @Autowired
    public Preferences preferences;

    @Autowired
    public Translator translator;

    @Getter
    public PopupMenu popupHeader;

    @Getter
    private final GridView<?> view;

    public GridTable(GridView<?> view) {
        this.view = view;
    }

    @Override
    public void initComponents() {
        super.initComponents();
        setSelectionType();
        setShowGrid(false);

        model.addTableModelListener(e -> invokeLater(() -> {
            int index = model.getCurrentIndex();
            if (index >= 0 && index < getRowCount()) {
                selectionModel.setSelectionInterval(index, index);
            }
        }));

        //setAutoCreateRowSorter(true);
    }

    @Override
    public void reset(final Streamer<T> parentStreamer) {
        super.reset(parentStreamer);
        getColumnModel().reset();
        setHeaderPopup();
        setColumnVisibility();
    }

    @Override
    protected TableColumnModel createDefaultColumnModel() {
        return new GridColumnModel();
    }

    @Override
    protected ListSelectionModel createDefaultSelectionModel() {
        return new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int rowIndex1, int rowIndex2) {
                if (getTableModel().isTopVisible()) {
                    if (rowIndex1 < rowIndex2 && rowIndex1 == 0) {
                        rowIndex1++;
                    } else if (rowIndex1 > rowIndex2 && rowIndex2 == 0) {
                        rowIndex2++;
                    }
                }
                super.setSelectionInterval(rowIndex1, rowIndex2);
                model.setCurrentIndex(rowIndex1);
                view.updateStatus();
            }
        };
    }

    @Override
    public GridColumnModel getColumnModel() {
        return (GridColumnModel) super.getColumnModel();
    }

    private void setSelectionType() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public void setHeaderPopup() {
        popupHeader = new PopupMenu();
        ofNullable(getParentStreamer())
            .ifPresent(parentStreamer -> {
                List<TranslatableName> detailNames = parentStreamer.detailNames();
                range(0, detailNames.size())
                    .mapToObj(index -> {
                        CheckBoxMenuItem checkbox = new CheckBoxMenuItem(
                            this, isColumnVisible(index), columnKey(index));
                        checkbox.setText(detailNames.get(index).name());
                        return checkbox;
                    })
                    .forEach(popupHeader::add);
            });
    }

    private boolean isColumnVisible(int index) {
        return applicationStorage.getBoolean(columnKey(index), true);
    }

    protected void setColumnVisibility() {
        range(0, getColumnCount())
            .filter(index -> index > 0)
            .forEach(index -> setColVisible(index, isColumnVisible(index)));
    }

    public void setColVisible(int index, boolean visible) {
        getColumnModel().setColVisible(index, visible);
    }

    public TableCellRenderer getCellRenderer(int row,
                                             int column) {
        return new GridCellRenderer();
    }

    @Override
    public void updateForm() {
        super.updateForm();

        setRowHeight(preferences.findIntPreference(ROW_HEIGHT)
            .orElse(CELL_ICON_SIZE));
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new GridHeader(getColumnModel());
    }

    @Override
    public GridTableModel<T> getTableModel() {
        return (GridTableModel<T>) super.getTableModel();
    }

    @Override
    protected DataTableModel<T> createDataTableModel() {
        return new GridTableModel<>();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() instanceof CheckBoxMenuItem menuItem) {
                String[] checkBoxKey = menuItem.getActionKey().split("\\|");
                int index = Integer.parseInt(checkBoxKey[checkBoxKey.length - 1]);
                setColVisible(index, menuItem.isSelected());
                applicationStorage.saveBoolean(columnKey(index), menuItem.isSelected());
            }
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }

    }

    private String columnKey(int index) {
        return storageKey("grid", "visible", Integer.toString(index));
    }

    @Override
    public void translate() {
        super.translate();
        if (popupHeader != null) {
            popupHeader.translate();
        }
    }
}
