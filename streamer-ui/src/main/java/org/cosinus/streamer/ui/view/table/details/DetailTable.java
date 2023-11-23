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

package org.cosinus.streamer.ui.view.table.details;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.menu.CheckBoxMenuItem;
import org.cosinus.swing.menu.PopupMenu;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.join;
import static java.util.Optional.ofNullable;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ROW_HEIGHT;
import static org.cosinus.streamer.ui.view.table.details.DetailView.DETAIL_VIEW_NAME;

public class DetailTable<T extends Streamable> extends DataTable<T> implements ActionListener {

    private static final int[] SIZE_COL = {200, 40, 50, 62, 110};

    @Autowired
    public ApplicationUIHandler uiHandler;

    @Autowired
    public IconHandler iconHandler;

    @Autowired
    public Preferences preferences;

    @Autowired
    public Translator translator;

    @Autowired
    public ApplicationStorage applicationStorage;

    //TODO
    private boolean keyboardArrow;

    public PopupMenu popupHeader;

    private final DetailView view;

    public DetailTable(DetailView view) {
        this.view = view;
    }

    public DetailView getView() {
        return view;
    }

    @Override
    public void initComponents() {
        super.initComponents();
        setHeader();
        setSelectionType();

        setSelectionModel(new DefaultListSelectionModel() {
            public void setSelectionInterval(int row1, int row2) {
                try {
                    super.setSelectionInterval(row1, row2);
                    getTableModel().setCurrentIndex(row1);
                } catch (Exception ex) {
                    errorHandler.handleError(DetailTable.this, ex);
                }
            }
        });
        setHeaderPopup();
    }

    private void setHeader() {
        TableColumnModel tcm = getColumnModel();
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setPreferredWidth(SIZE_COL[i]);
            tcm.getColumn(i).setHeaderRenderer(new DetailHeaderCell());
            if (i > 0) {
                setColVisible(i, isColumnVisible(i));
            }
        }
        getTableHeader().setReorderingAllowed(false);
    }

    private void setSelectionType() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel selectionModel = getSelectionModel();
        selectionModel.addListSelectionListener(event -> {
            try {
                ListSelectionModel lsm = (ListSelectionModel) event.getSource();
                if (lsm.isSelectionEmpty()) {
                    return;
                }
                if (event.getValueIsAdjusting()) {
                    return;
                }

                int first = event.getFirstIndex();
                int last = event.getLastIndex();
                if (first == last) {
                    return;
                }

                int selectedRow = lsm.getMinSelectionIndex();
                int oldRow = selectedRow == first ? last : first;
                if (shiftDown) {
                    int start = min(oldRow, selectedRow);
                    int end = max(oldRow, selectedRow);
                    selectItems(start,
                        end,
                        true,
                        false);
                } else if (ctrlDown && !keyboardArrow) {
                    selectIndexAtIndex(selectedRow);
                }
            } catch (Exception ex) {
                errorHandler.handleError(DetailTable.this, ex);
            }
        });
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent keyEvent) {
        keyboardArrow = true;
        super.processComponentKeyEvent(keyEvent);
        invokeLater(() -> keyboardArrow = true);
    }

    private void setHeaderPopup() {
        popupHeader = new PopupMenu();
        ofNullable(getParentStreamer())
            .ifPresent(parentStreamer -> {
                List<TranslatableName> detailName = parentStreamer.detailNames();
                IntStream.range(1, detailName.size())
                    .mapToObj(index -> new CheckBoxMenuItem(
                        this, isColumnVisible(index), columnKey(index)))
                    .forEach(popupHeader::add);
            });
    }

    private boolean isColumnVisible(int index) {
        return applicationStorage.getBoolean(columnKey(index), true);
    }

    private String columnKey(int index) {
        return ofNullable(getParentStreamer())
            .map(Streamer::getId)
            .map(id -> join("|", DETAIL_VIEW_NAME, id, Integer.toString(index)))
            .orElseGet(() -> join("|", DETAIL_VIEW_NAME, Integer.toString(index)));
    }

    public void setColVisible(int index,
                              boolean visible) {
        TableColumnModel tcm = getColumnModel();
        TableColumn col = tcm.getColumn(index);

        if (visible) {
            col.setMaxWidth(10000);
            col.setMinWidth(5);
            col.setPreferredWidth(SIZE_COL[index]);
            col.setWidth(SIZE_COL[index]);
            col.setResizable(true);
        } else {
            col.setMaxWidth(0);
            col.setMinWidth(0);
            col.setPreferredWidth(0);
            col.setWidth(0);
            col.setResizable(false);
        }
    }

    public TableCellRenderer getCellRenderer(int row,
                                             int column) {
        return new DetailCellRenderer();
    }

    @Override
    public void updateForm() {
        super.updateForm();

        setRowHeight(preferences.findIntPreference(ROW_HEIGHT)
            .orElse(20));
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new DetailHeader(getColumnModel());
    }

    @Override
    public void setCurrentIndex(int index) {
        getSelectionModel().setSelectionInterval(index, index);
        scrollRectToVisible(getCellRect(index, 0, false));
        repaint();
    }

    @Override
    public DetailTableModel getTableModel() {
        return (DetailTableModel) super.getTableModel();
    }

    @Override
    protected DataTableModel<T> createDataTableModel() {
        return new DetailTableModel<>(view.getParentStreamer());
    }

    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() instanceof CheckBoxMenuItem menuItem) {
                String[] checkBoxKey = menuItem.getActionKey().split("\\|");
                int columnIndex = Integer.parseInt(checkBoxKey[checkBoxKey.length - 1]);
                setColVisible(columnIndex, menuItem.isSelected());
            }
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }

    }

    public PopupMenu getPopupHeader() {
        return popupHeader;
    }

    @Override
    public void translate() {
        super.translate();
        if (popupHeader != null) {
            popupHeader.translate();
        }
    }
}
