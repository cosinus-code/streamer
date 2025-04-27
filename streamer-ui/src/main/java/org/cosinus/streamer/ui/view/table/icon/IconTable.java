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
import org.cosinus.swing.image.icon.IconSize;
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
import static org.cosinus.swing.image.icon.IconSize.X32;
import static org.cosinus.swing.math.MoreMath.divideAndFloor;

public class IconTable<T extends Streamable> extends DataTable<T> {

    public static final int PREVIEW_CELL_SIZE = 100;

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private Preferences preferences;

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Override
    public void initComponents() {
        super.initComponents();
        setHeader();
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

    private void setHeader() {
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

    @Override
    public void addIndexToSelection(int index) {
        //TODO: to add to selection
        setCurrentIndex(index);
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
        if (index < 0) {
            row = 0;
            column = 0;
        } else {
            int count = getTableModel().getItemsCount();
            if (index >= count) {
                row = getTableModel().getRowForIndex(count - 1);
                column = getTableModel().getColumnForIndex(count - 1);
            }
        }

        super.changeSelection(row, column, toggle, extend);
    }

    public int getCellWidth() {
        return getIconDimension() + (uiHandler.isLookAndFeelWindows() ? 8 : 52);
    }

    public int getCellHeight() {
        if (preferences.booleanPreference(PREVIEW)) {
            int rowHeight = preferences.intPreference(ROW_HEIGHT);
            return getIconDimension() + rowHeight + (uiHandler.isLookAndFeelWindows() ? 21 : 14);
        }

        return getIconDimension();
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