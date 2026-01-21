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

package org.cosinus.streamer.ui.view.table.details;

import lombok.Getter;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.streamer.ui.view.table.details.DetailsCellRenderer.ROW_HEIGHT;

public class DetailsTable<T extends Streamable> extends DataTable<T> {

    @Autowired
    public ApplicationUIHandler uiHandler;

    @Autowired
    public Preferences preferences;

    @Autowired
    public Translator translator;

    @Getter
    private final DetailsView<?> view;

    public DetailsTable(DetailsView<?> view) {
        this.view = view;
    }

    @Override
    public void initComponents() {
        super.initComponents();
        setSelectionType();
        setShowGrid(false);
        hideHeader();

        model.addTableModelListener(e -> invokeLater(() -> {
            int index = model.getCurrentIndex();
            if (index >= 0 && index < getRowCount()) {
                selectionModel.setSelectionInterval(index, index);
            }
        }));
    }

    private void hideHeader() {
        setTableHeader(null);
    }

    @Override
    protected ListSelectionModel createDefaultSelectionModel() {
        return new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int rowIndexStart, int rowIndexEnd) {
                if (getTableModel().isTopVisible()) {
                    if (rowIndexStart < rowIndexEnd && rowIndexStart == 0) {
                        rowIndexStart++;
                    } else if (rowIndexStart > rowIndexEnd && rowIndexEnd == 0) {
                        rowIndexEnd++;
                    }
                }
                super.setSelectionInterval(rowIndexStart, rowIndexEnd);
                model.setCurrentIndex(rowIndexStart);
                view.updateStatus();
            }
        };
    }

    private void setSelectionType() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        return new DetailsCellRenderer();
    }

    @Override
    public void updateForm() {
        super.updateForm();

        setRowHeight(ROW_HEIGHT);
    }

    @Override
    public DetailsTableModel<T> getTableModel() {
        return (DetailsTableModel<T>) super.getTableModel();
    }

    @Override
    protected DataTableModel<T> createDataTableModel() {
        return new DetailsTableModel<>();
    }
}
