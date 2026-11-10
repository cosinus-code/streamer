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

/*
 * IconTable.java
 *
 * Created on July 13, 2005, 12:53 PM
 */

package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.*;
import static org.cosinus.swing.icon.IconSize.X32;
import static org.cosinus.swing.math.MoreMath.divideAndFloor;

public class IconTable<T extends Streamable> extends DataTable<T> {

    public static final int PREVIEW_CELL_SIZE = 100;

    /**
     * The ratio of the cell width to the icon size, so the cells keep their proportions when the icon size changes.
     */
    public static final double CELL_WIDTH_TO_ICON_RATIO = 2;

    /**
     * The ratio of the cell width to the icon size with the Windows look and feel.
     */
    public static final double CELL_WIDTH_TO_ICON_RATIO_WINDOWS = 1.25;

    /**
     * The ratio of the cell height to the icon size, so the cells keep their proportions when the icon size changes;
     * a row is still enlarged when the name below the icon needs more room.
     */
    public static final double CELL_HEIGHT_TO_ICON_RATIO = 2;

    /**
     * The minimum cell width, so the names below small icons are not wrapped on too many lines.
     */
    public static final int MIN_CELL_WIDTH = 64;

    /**
     * The width added to the preview size for the cell width in preview mode.
     */
    public static final int PREVIEW_CELL_WIDTH_PADDING = 52;

    /**
     * The width added to the preview size for the cell width in preview mode, with the Windows look and feel.
     */
    public static final int PREVIEW_CELL_WIDTH_PADDING_WINDOWS = 8;

    /**
     * The height added to the preview size and the row height for the cell height in preview mode.
     */
    public static final int PREVIEW_CELL_HEIGHT_PADDING = 14;

    /**
     * The height added to the preview size and the row height for the cell height in preview mode,
     * with the Windows look and feel.
     */
    public static final int PREVIEW_CELL_HEIGHT_PADDING_WINDOWS = 21;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private Preferences preferences;

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Override
    public void initComponents() {
        super.initComponents();
        hideHeader();
        setSelectionType();
    }

    @Override
    protected DataTableModel<T> createDataTableModel() {
        return new IconTableModel<>();
    }

    @Override
    public IconTableModel<T> getTableModel() {
        return (IconTableModel<T>) super.getTableModel();
    }

    private void hideHeader() {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setTableHeader(null);
    }

    private void setSelectionType() {
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        ListSelectionListener listSelectionListener = new IconListSelectionListener();
        getColumnModel().getSelectionModel().addListSelectionListener(listSelectionListener);
        getSelectionModel().addListSelectionListener(listSelectionListener);
    }

    @Override
    public IconCellRenderer getCellRenderer(int row, int column) {
        return new IconCellRenderer(this);
    }

    @Override
    public void setCurrentIndex(int index) {
        int row = getTableModel().getRowForIndex(index);
        int column = getTableModel().getColumnForIndex(index);
        changeSelection(row, column, false, false);
        scrollRectToVisible(getCellRect(row, column, false));
        repaint();
    }

    @Override
    public void addIndexToSelection(int index) {
        //TODO: to add to selection
        setCurrentIndex(index);
    }

    @Override
    public void extendsSelectionToIndex(int index) {
        //TODO: to add to selection
        setCurrentIndex(index);
    }

    @Override
    protected TableColumnModel createDefaultColumnModel() {
        return new DefaultTableColumnModel() {
            @Override
            protected ListSelectionModel createSelectionModel() {
                return new DefaultListSelectionModel() {
                    @Override
                    public void setSelectionInterval(int columnIndex1, int columnIndex2) {
                        if (getTableModel().isTopVisible() &&
                            IconTable.this.getSelectionModel().isSelectedIndex(0)) {
                            if (columnIndex1 < columnIndex2 && columnIndex1 == 0) {
                                columnIndex1++;
                            } else if (columnIndex1 > columnIndex2 && columnIndex2 == 0) {
                                columnIndex2++;
                            }
                        }
                        super.setSelectionInterval(columnIndex1, columnIndex2);
                    }
                };
            }
        };
    }

    public void onResize(int width, int height) {
        super.onResize(width, height);
        getTableModel().setColumnCount(divideAndFloor(width, getCellWidth()));

        setRowHeight(getCellHeight());

        TableColumnModel tcm = getColumnModel();
        range(0, tcm.getColumnCount())
            .mapToObj(tcm::getColumn)
            .forEach(column -> column.setPreferredWidth(getCellWidth()));
    }

    @Override
    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        int index = getTableModel().getIndex(row, column);
        int newRow;
        int newColumn;
        if (index < 0) {
            newRow = 0;
            newColumn = 0;
        } else {
            int count = getTableModel().getItemsCount();
            if (index >= count) {
                newRow = getTableModel().getRowForIndex(count - 1);
                newColumn = getTableModel().getColumnForIndex(count - 1);
            } else {
                newRow = row;
                newColumn = column;
            }
        }

        super.changeSelection(newRow, newColumn, toggle, extend);
    }

    /**
     * @return the cell width: proportional to the icon size, but at least {@value #MIN_CELL_WIDTH},
     * or the preview size plus a padding in preview mode
     */
    public int getCellWidth() {
        boolean windows = uiHandler.isLookAndFeelWindows();
        if (preferences.booleanPreference(PREVIEW)) {
            return PREVIEW_CELL_SIZE +
                (windows ? PREVIEW_CELL_WIDTH_PADDING_WINDOWS : PREVIEW_CELL_WIDTH_PADDING);
        }
        return Math.max(MIN_CELL_WIDTH, (int) Math.round(getIconDimension() *
            (windows ? CELL_WIDTH_TO_ICON_RATIO_WINDOWS : CELL_WIDTH_TO_ICON_RATIO)));
    }

    /**
     * @return the cell height: proportional to the icon size,
     * or the preview size plus the row height and a padding in preview mode
     */
    public int getCellHeight() {
        if (preferences.booleanPreference(PREVIEW)) {
            return PREVIEW_CELL_SIZE + preferences.intPreference(ROW_HEIGHT) +
                (uiHandler.isLookAndFeelWindows() ? PREVIEW_CELL_HEIGHT_PADDING_WINDOWS : PREVIEW_CELL_HEIGHT_PADDING);
        }
        return (int) Math.round(getIconDimension() * CELL_HEIGHT_TO_ICON_RATIO);
    }

    public int getIconDimension() {
        return preferences.booleanPreference(PREVIEW) ? PREVIEW_CELL_SIZE : getIconSize().getSize();
    }

    public IconSize getIconSize() {
        return preferences.findStringPreference(ICON_SIZE)
            .flatMap(IconSize::forText)
            .orElse(X32);
    }

    private class IconListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent event) {
            try {
                ListSelectionModel selectionModel = (ListSelectionModel) event.getSource();
                if (selectionModel.isSelectionEmpty() || event.getValueIsAdjusting()) {
                    return;
                }

                int currentIndex = getTableModel().getIndex(getSelectedRow(), getSelectedColumn());
                getTableModel().setCurrentIndex(currentIndex);
            } catch (Exception ex) {
                errorHandler.handleError(ex);
            }
        }
    }
}