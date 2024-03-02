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

import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.streamer.ui.view.table.TableCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.image.icon.IconSize.X16;

public class DetailCellRenderer extends TableCellRenderer<DetailTable> {

    @Override
    public Component getCellComponent(JLabel label,
                                      DetailTable table,
                                      ViewItem item,
                                      boolean isSelected,
                                      boolean hasFocus,
                                      int row,
                                      int column) {

        setBorder(emptyBorder(0, 3, 0, 3));

        label.setText(item.isTopItem() ? item.toString() : ofNullable(item.getDetail(column))
            .map(Object::toString)
            .orElse(""));

        if (column == 0) {
            Optional<Icon> icon = item.isTopItem() ?
                getUpIcon(X16) :
                getIcon(X16, item);
            icon.ifPresent(label::setIcon);
        }

        return label;
    }
}
