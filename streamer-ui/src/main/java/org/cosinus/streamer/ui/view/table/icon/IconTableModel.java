/*
 * IconTableModel.java
 *
 * Created on July 13, 2005, 12:53 PM
 */

package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.table.DataTableModel;

import static org.cosinus.swing.math.MoreMath.divideAndFloor;

public class IconTableModel<T extends Streamable> extends DataTableModel<T> {

    private int columnCount = 0;

    public IconTableModel(final Streamer<T> parentStreamer) {
        super(parentStreamer);
    }

    @Override
    public int getRowCount() {
        return getColumnCount() > 0 ? getRowForIndex(viewItems.size() - 1) + 1 : 0;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int row, int column) {
        int index = getIndex(row, column);
        return index >= 0 && index < viewItems.size() ? viewItems.get(index) : null;
    }

    @Override
    public int getRowForIndex(int index) {
        return getColumnCount() > 0 ? divideAndFloor(index, getColumnCount()) : 0;
    }

    @Override
    public int getColumnForIndex(int index) {
        return getColumnCount() > 0 ? index % getColumnCount() : index;
    }

    @Override
    public int getIndex(int row, int column) {
        return row * getColumnCount() + column;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount > 0 ? columnCount : 1;
        fireTableStructureChanged();
    }

    @Override
    public void translate() {
    }

}