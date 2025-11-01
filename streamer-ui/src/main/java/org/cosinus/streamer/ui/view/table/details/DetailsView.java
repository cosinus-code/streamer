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

package org.cosinus.streamer.ui.view.table.details;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.TableStreamerView;

import java.awt.*;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.view.table.details.DetailsCellRenderer.ICON_SIZE;

public class DetailsView<T extends Streamable> extends TableStreamerView<T> {

    public static final String DETAILS_VIEW_NAME = "details";

    public DetailsView(PanelLocation location) {
        super(location);
    }

    @Override
    public String getName() {
        return DETAILS_VIEW_NAME;
    }

    @Override
    protected DataTable<T> createDataTable() {
        return new DetailsTable<>(this);
    }

    @Override
    protected DetailsStreamerEditor<T> createStreamerEditor() {
        return new DetailsStreamerEditor<>(this);
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || ofNullable(streamerEditor)
            .map(StreamerEditor::isDirty)
            .orElse(false);
    }

    public Rectangle getCurrentCellRectangle() {
        int index = table.getCurrentIndex();
        int row = table.getTableModel().getRowForIndex(index);

        Rectangle rect = table.getCellRect(row, 0, true);
        int offsetIcon = ICON_SIZE.getSize() + 4;
        return new Rectangle(rect.x + offsetIcon, rect.y, rect.width - offsetIcon, rect.height);
    }

}
