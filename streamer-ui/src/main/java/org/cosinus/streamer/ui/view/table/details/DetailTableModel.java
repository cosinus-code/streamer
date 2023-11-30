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
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public class DetailTableModel<T extends Streamable> extends DataTableModel<T> {

    @Autowired
    public Translator translator;

    private List<TranslatableName> columnNames = new ArrayList<>();

    private int sortedColumn = -1;

    //TODO: to see if this is still needed
    private final Map<String, Long> mapComputedSize;

    public DetailTableModel() {
        this.mapComputedSize = new HashMap<>();
    }

    @Override
    public void reset(final Streamer<T> parentStreamer) {
        super.reset(parentStreamer);
        resetHeader();
    }

    public void resetHeader() {
        this.columnNames = ofNullable(parentStreamer.detailNames())
            .filter(not(List::isEmpty))
            .orElseGet(() -> singletonList(getName()));
        translate();
        fireTableStructureChanged();
    }

    public int getSortedColumn() {
        return sortedColumn;
    }

    public void setSortedColumn(int sortedColumn) {
        this.sortedColumn = sortedColumn;
    }

    @Override
    public int getRowCount() {
        return viewItems.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return this.columnNames.get(column).name();
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row >= viewItems.size()) {
            return null;
        }
        ViewItem item = viewItems.get(row);
        if (item.isTopItem() && column > 0) {
            return "";
        }

        return item;
    }

    //TODO
    private boolean isComputingSize(String path) {
        return mapComputedSize.containsKey(path);
    }

    @Override
    public int getRowForIndex(int index) {
        return index;
    }

    @Override
    public int getColumnForIndex(int index) {
        return 0;
    }

    @Override
    public int getIndex(int row, int column) {
        return row;
    }

//    @Override
//    protected void addItem(ViewItem viewItem) {
//        super.addItem(viewItem);
//        ofNullable(viewItem.getId())
//            .map(streamableMap::get)
//            .ifPresent(Streamable::initDetails);
//    }

    private TranslatableName getName() {
        return new TranslatableName("form-table-header-name", null);
    }

    @Override
    public void translate() {
        columnNames.forEach(TranslatableName::translate);
    }
}
