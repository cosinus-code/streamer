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

import static java.util.Optional.ofNullable;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ROW_HEIGHT;

public class DetailTable<T extends Streamable> extends DataTable<T> implements ActionListener {

    @Autowired
    public ApplicationUIHandler uiHandler;

    @Autowired
    public IconHandler iconHandler;

    @Autowired
    public Preferences preferences;

    @Autowired
    public Translator translator;

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
        setSelectionType();

        setSelectionModel(new DefaultListSelectionModel() {
            public void setSelectionInterval(int row1, int row2) {
                try {
                    super.setSelectionInterval(row1, row2);
                    model.setCurrentIndex(row1);
                } catch (Exception ex) {
                    errorHandler.handleError(DetailTable.this, ex);
                }
            }
        });
    }

    @Override
    public void reset(final Streamer<T> parentStreamer) {
        super.reset(parentStreamer);
        setHeaderRenders();
        setHeaderPopup();
    }

    private void setHeaderRenders() {
        TableColumnModel model = getColumnModel();
        IntStream.range(0, model.getColumnCount())
            .forEach(index -> {
                model.getColumn(index).setHeaderRenderer(new DetailHeaderCell());
                if (index > 0) {
                    setColVisible(index, isColumnVisible(index));
                }
            });
        getTableHeader().setReorderingAllowed(false);
    }

    private void setSelectionType() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent keyEvent) {
        keyboardArrow = true;
        super.processComponentKeyEvent(keyEvent);
        invokeLater(() -> keyboardArrow = true);
    }

    public void setHeaderPopup() {
        popupHeader = new PopupMenu();
        ofNullable(getParentStreamer())
            .ifPresent(parentStreamer -> {
                List<TranslatableName> detailNames = parentStreamer.detailNames();
                IntStream.range(0, detailNames.size())
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

    public void setColVisible(int index,
                              boolean visible) {
        TableColumn column = getColumnModel().getColumn(index);
        if (visible) {
            column.setMaxWidth(10000);
            column.setMinWidth(5);
            column.setPreferredWidth(100);
            column.setWidth(100);
            column.setResizable(true);
        } else {
            column.setMaxWidth(0);
            column.setMinWidth(0);
            column.setPreferredWidth(0);
            column.setWidth(0);
            column.setResizable(false);
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
        getTableModel().setCurrentIndex(index);
        scrollRectToVisible(getCellRect(index, 0, false));
        repaint();
    }


    @Override
    public void selectCurrentIndex(int index) {
        getSelectionModel().addSelectionInterval(index, index);
        getTableModel().setCurrentIndex(index);
        scrollRectToVisible(getCellRect(index, 0, false));
        repaint();
    }

    @Override
    public DetailTableModel getTableModel() {
        return (DetailTableModel) super.getTableModel();
    }

    @Override
    protected DataTableModel<T> createDataTableModel() {
        return new DetailTableModel<>();
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
        return storageKey("detail", "visible", Integer.toString(index));
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
