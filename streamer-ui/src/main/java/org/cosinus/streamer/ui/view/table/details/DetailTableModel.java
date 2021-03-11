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

import org.cosinus.streamer.api.Element;
import org.cosinus.streamer.ui.preference.StreamerPreferences;
import org.cosinus.streamer.ui.view.table.DataTableModel;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.context.SwingAutowired;
import org.cosinus.swing.translate.Translator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.cosinus.streamer.ui.util.Formatter.formatMemorySize;

public class DetailTableModel extends DataTableModel {

    public static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    private final String[] columnNames = new String[DetailColumn.values().length];

    //TODO: to see if this is still needed
    private final Map<String, Long> mapComputedSize;

    @SwingAutowired
    public Translator translator;

    public DetailTableModel() {
        this.mapComputedSize = new HashMap<>();
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row,
                             int column) {
        if (row >= items.size()) {
            return null;
        }
        ViewItem item = items.get(row);
        if (item.isTopElement() && column > 0) {
            return "";
        }
        DetailColumn col = DetailColumn.getValueAt(column);
        switch (col) {
            case VALUE:
                return item.getStreamer().getValue();
            case TYPE:
                return preferences.booleanPreference(StreamerPreferences.OPTION_FULL_TYPE_DESCR) ?
                    item.getStreamer().getDescription() :
                    item.getStreamer().getType();
            case SIZE:
                return isComputingSize(item.getStreamer().getPath().toString()) ?
                    "..." :
                    getFormattedSize(item.getStreamer());
            case TIME:
                return getFormattedDate(item.getStreamer().lastModified());
            default:
                return item;
        }
    }

    private String getFormattedSize(Element element) {
        return element.isDirectory() ? "" : formatMemorySize(element.getSize());
    }

    private String getFormattedDate(long date) {
        return DATE_FORMAT.format(new Date(date));
    }

    private boolean isComputingSize(String path) {
        return mapComputedSize.containsKey(path);
    }

//    @Override
//    public void addElement(int index) {
//        fireTableRowsUpdated(index,
//                             index);
//    }

    @Override
    public int getRowForIndex(int index) {
        return index;
    }

    @Override
    public int getColumnForIndex(int index) {
        return 0;
    }

    @Override
    public int getIndex(int row,
                        int column) {
        return row;
    }

    @Override
    public void initComponents() {

    }

    @Override
    public void translate() {
        stream(DetailColumn.values())
            .forEach(column -> columnNames[column.ordinal()] = translator.translate(column.key()));
    }

    @Override
    public void initContent() {

    }
}
