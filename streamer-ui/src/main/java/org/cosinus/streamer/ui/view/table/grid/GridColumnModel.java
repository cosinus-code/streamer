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

import org.cosinus.streamer.ui.view.table.grid.header.GridHeaderCellCreator;
import org.cosinus.swing.context.SwingAutowired;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import static java.util.stream.IntStream.range;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * The grid colum model
 */
public class GridColumnModel extends DefaultTableColumnModel {

    @SwingAutowired
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
