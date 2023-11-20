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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.streamer.ui.view.table.StreamerViewItem;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

public class DetailTableModel<T extends Streamer<?>> extends DataTableModel<T> {

    @Autowired
    public Translator translator;

    private final List<TranslatableName> columnNames;

    //TODO: to see if this is still needed
    private final Map<String, Long> mapComputedSize;

    public DetailTableModel(final Streamer<T> parentStreamer) {
        super(parentStreamer);
        this.mapComputedSize = new HashMap<>();
        this.columnNames = ofNullable(parentStreamer.detailNames())
            .filter(not(List::isEmpty))
            .orElseGet(() -> singletonList(getName()));
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
    public Object getValueAt(int row,
                             int column) {
        if (row >= viewItems.size()) {
            return null;
        }
        StreamerViewItem item = viewItems.get(row);
        if (item.isTopItem() && column > 0) {
            return "";
        }

        return column > 0 ? item.getDetail(column) : item;

//        DetailColumn col = DetailColumn.getValueAt(column);
//        switch (col) {
//            case VALUE:
//                return item.getStreamer().getValue();
//            case TYPE:
//                return preferences.booleanPreference(FULL_TYPE_DESCRIPTION) ?
//                    item.getStreamer().getDescription() :
//                    item.getStreamer().getType();
//            case SIZE:
//                return isComputingSize(item.getStreamer().getPath().toString()) ?
//                    "..." :
//                    item.getFormattedSize();
//            case TIME:
//                return getFormattedDate(item.getStreamer().lastModified());
//            default:
//                return item;
//        }
    }

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

    private TranslatableName getName() {
        return new TranslatableName("form-table-header-name", null);
    }

    @Override
    public void translate() {
        columnNames.forEach(TranslatableName::translate);
    }
}
