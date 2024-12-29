package org.cosinus.streamer.ui.view.table.grid;

import org.cosinus.streamer.ui.view.table.grid.header.GridHeaderCellCreator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import static java.util.stream.IntStream.range;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * The grid colum model
 */
public class GridColumnModel extends DefaultTableColumnModel {

    @Autowired
    protected GridHeaderCellCreator gridHeaderCellCreator;

    public GridColumnModel() {
        injectContext(this);
    }

    @Override
    public TableColumn getColumn(int columnIndex) {
        try {
            return super.getColumn(columnIndex);
        } catch (Exception e) {
            //TODO: sometimes it throws IndexOutOfBoundsException
            return new TableColumn();
        }
    }

    /**
     * Reset this column model
     */
    public void reset() {
        range(0, getColumnCount())
            .mapToObj(this::getColumn)
            .forEach(column -> column.setHeaderRenderer(createHeaderCellRenderer()));
    }

    protected TableCellRenderer createHeaderCellRenderer() {
        return gridHeaderCellCreator.createGridHeaderCellRenderer();
    }

    public void setColVisible(int index, boolean visible) {
        TableColumn column = getColumn(index);
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
}
