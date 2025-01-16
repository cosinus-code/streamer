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

package org.cosinus.streamer.ui.view.table.grid;

import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.ui.view.table.TableCellRenderer;
import org.cosinus.streamer.ui.view.table.ViewItem;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.image.icon.IconSize.X16;

public class GridCellRenderer extends TableCellRenderer<GridTable> {

    public static final int CELL_ICON_SIZE = 22;

    public static final int CELL_HORIZONTAL_MARGIN = 3;

    public static final Border CELL_BORDER = emptyBorder(0, CELL_HORIZONTAL_MARGIN, 0, CELL_HORIZONTAL_MARGIN);

    @Override
    public Component getCellComponent(JLabel label,
                                      GridTable table,
                                      ViewItem item,
                                      boolean isSelected,
                                      int row,
                                      int column) {

        setBorder(CELL_BORDER);

        label.setText(item.isTopItem() ?
            item.toString() :
            ofNullable(item.getDetail(column))
                .map(Object::toString)
                .orElse(""));
        ofNullable(item.getDetail(column))
            .filter(Value::isNumeric)
            .ifPresent(value -> label.setHorizontalAlignment(RIGHT));

        if (column == 0) {
            Optional<Icon> icon = item.isTopItem() ?
                getUpIcon(X16) :
                getIcon(X16, item);
            icon.ifPresent(label::setIcon);
        }

        return label;
    }
}
