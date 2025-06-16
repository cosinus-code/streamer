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

package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.table.TableStreamerView;

import javax.swing.*;

public class IconView<T extends Streamable> extends TableStreamerView<T> {

    public static final String ICON_VIEW_NAME = "icon";

    private IconCellEditor<T> iconCellEditor;

    public IconView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();
    }

    @Override
    public String getName() {
        return ICON_VIEW_NAME;
    }

    @Override
    protected IconTable<T> createDataTable() {
        return new IconTable<>();
    }

    @Override
    protected StreamerEditor<T> createStreamerEditor() {
        if (iconCellEditor == null) {
            iconCellEditor = new IconCellEditor<>(this);
            getContainer().add(iconCellEditor.getNameEditor());
        }
        return iconCellEditor;
    }

    public JTextPane resetCellEditor(JTextPane nameEditor) {
        int index = table.getCurrentIndex();
        int row = table.getTableModel().getRowForIndex(index);
        int column = table.getTableModel().getColumnForIndex(index);

        return ((IconCellRenderer) table.getCellRenderer(row, column)).resetCellEditor(nameEditor, row, column);
    }
}
