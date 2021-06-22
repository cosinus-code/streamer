/*
 * IconTable.java
 *
 * Created on July 13, 2005, 12:53 PM
 */

package org.cosinus.streamer.ui.view.table.icon;

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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.*;
import static org.cosinus.swing.image.icon.IconSize.X32;
import static org.cosinus.swing.math.MoreMath.*;

public class IconTable extends DataTable {

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

        ActionMap am = getActionMap();
        am.put("selectPreviousColumnCell", new PreviousFocusHandler());
        am.put("selectNextColumnCell", new NextFocusHandler());
    }

    public class PreviousFocusHandler extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.focusPreviousComponent();
        }
    }

    public class NextFocusHandler extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.focusNextComponent();
        }
    }

    @Override
    protected DataTableModel createDataTableModel() {
        return new IconTableModel();
    }

    @Override
    protected IconTableModel getTableModel() {
        return (IconTableModel) super.getTableModel();
    }

    private void setHeader() {
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setTableHeader(null);
    }

    private void setSelectionType() {
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setCellSelectionEnabled(true);

        ListSelectionListener listSelectionListener = new IconListSelectionListener();
        getColumnModel().getSelectionModel().addListSelectionListener(listSelectionListener);
        getSelectionModel().addListSelectionListener(listSelectionListener);
    }

    public TableCellRenderer getCellRenderer(int row, int column) {
        return new IconCellRenderer();
    }

    public void setCurrentIndex(int index) {
        int row = getTableModel().getRowForIndex(index);
        int column = getTableModel().getColumnForIndex(index);
        changeSelection(row, column, false, false);
        scrollRectToVisible(getCellRect(row, column, false));
        repaint();
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
        return getIconDimension() + (uiHandler.isLookAndFeelWindows() ? 8 : 42);
    }

    public int getCellHeight() {
        if (preferences.booleanPreference(PREVIEW)) {
            int rowHeight = preferences.intPreference(ROW_HEIGHT);
            return getIconDimension() + rowHeight + (uiHandler.isLookAndFeelWindows() ? 21 : 14);
        }
        if (uiHandler.isLookAndFeelWindows()) {
            return getIconDimension();
        }
        if (uiHandler.isLookAndFeelGTK()) {
            return getIconDimension() - 8;
        }

        return getIconDimension();
    }

    public int getIconDimension() {
        return (preferences.booleanPreference(PREVIEW) ? PREVIEW_CELL_SIZE : getIconSize().getSize()) + 30;
    }

    public IconSize getIconSize() {
        return preferences.findStringPreference(ICON_SIZE)
            .flatMap(IconSize::forText)
            .orElse(X32);
    }

    public ArrayList<Integer> getIndexesInRect(Point pointStart, Point pointEnd) {
        Rectangle rect = getVisibleRect();
        int minx = min(pointStart.x, pointEnd.x) + rect.x;
        int maxx = max(pointStart.x, pointEnd.x) + rect.x;
        int miny = min(pointStart.y, pointEnd.y) + rect.y;
        int maxy = max(pointStart.y, pointEnd.y) + rect.y;

        int colStart = divideAndCeil(minx, getCellWidth());
        int colEnd = divideAndCeil(maxx, getCellWidth());
        int rowStart = divideAndCeil(miny, getCellHeight());
        int rowEnd = divideAndCeil(maxy, getCellHeight());

        int rows = getTableModel().getRowCount();
        if (rowEnd >= rows) rowEnd = rows - 1;

        int cols = getTableModel().getColumnCount();
        if (colEnd >= cols) colEnd = cols - 1;

        ArrayList<Integer> indexes = new ArrayList<>();
        for (int row = rowStart; row <= rowEnd; row++) {
            for (int col = colStart; col <= colEnd; col++) {
                indexes.add(getTableModel().getIndex(row, col));
            }
        }
        return indexes;
    }

    public int getClosestIndex(Point point) {
        Rectangle rect = getVisibleRect();
        int col = divideAndCeil(point.x + rect.x, getCellWidth());
        int row = divideAndCeil(point.y + rect.y, getCellHeight());

        return getTableModel().getIndex(fitInRange(row, 0, getTableModel().getRowCount()),
                                        fitInRange(col, 0, getTableModel().getColumnCount()));
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