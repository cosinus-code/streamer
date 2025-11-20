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

import org.cosinus.streamer.ui.view.table.TableCellRenderer;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.text.HtmlText;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.icon.IconSize.X16;
import static org.cosinus.swing.icon.IconSize.X32;

public class DetailsCellRenderer extends TableCellRenderer<DetailsTable> {

    public static final IconSize ICON_SIZE = X32;

    public static final int ROW_HEIGHT = 3 * X16.getSize();

    public static final int CELL_HORIZONTAL_MARGIN = 3;

    public static final Border CELL_BORDER = emptyBorder(0, CELL_HORIZONTAL_MARGIN, 0, CELL_HORIZONTAL_MARGIN);

    @Autowired
    protected FormatHandler formatHandler;

    @Override
    public Component getCellComponent(JLabel label,
                                      DetailsTable table,
                                      ViewItem item,
                                      boolean isSelected,
                                      int row,
                                      int column) {

        setBorder(CELL_BORDER);

        ofNullable(item)
            .filter(ViewItem::isTopItem)
            .flatMap(i -> getUpIcon(ICON_SIZE))
            .or(() -> getIcon(ICON_SIZE, item))
            .ifPresent(label::setIcon);

        ofNullable(item)
            .map(Objects::toString)
            .ifPresent(label::setText);

        label.setText(new HtmlText() {
            @Override
            public String getHtml() {
                return htmlText(wrappedHtml(
                    formatHandler.formatTextForLabel(table, boldText(label.getText())),
                    formatHandler.formatTextForLabel(table,
                        ofNullable(item)
                            .filter(not(ViewItem::isTopItem))
                            .map(ViewItem::getDescription)
                            .orElse(""))));
            }
        }.getHtml());

        return label;
    }
}
