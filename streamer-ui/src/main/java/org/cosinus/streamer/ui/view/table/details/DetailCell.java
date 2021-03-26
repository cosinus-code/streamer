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

import org.cosinus.streamer.ui.preference.StreamerPreferences;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.image.icon.IconSize;
import org.cosinus.swing.preference.Preferences;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Optional;

import static org.cosinus.swing.image.icon.IconProvider.ICON_FOLDER;
import static org.cosinus.swing.image.icon.IconProvider.ICON_UP;
import static org.cosinus.swing.image.icon.IconSize.X16;

public class DetailCell extends DefaultTableCellRenderer {

    private final Preferences preferences;

    private final IconHandler iconHandler;

    public DetailCell(Preferences preferences,
                      IconHandler iconHandler) {
        this.preferences = preferences;
        this.iconHandler = iconHandler;
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {

        Component c = super.getTableCellRendererComponent(table,
                value,
                isSelected,
                hasFocus,
                row,
                column);

        if (!(c instanceof JLabel)) {
            return c;
        }

        JLabel label = (JLabel) c;
        setBorder(BorderFactory.createEmptyBorder(0,
                3,
                0,
                3));

        boolean isNumeric = column == DetailColumn.SIZE.ordinal();
        label.setHorizontalAlignment(isNumeric ? JLabel.RIGHT : JLabel.LEFT);

        DetailTable detailTable = (DetailTable) table;
        if (detailTable.isIndexSelected(row)) {
            preferences.findColorPreference(StreamerPreferences.OPTION_SELECT_FOREGROUND)
                    .ifPresent(this::setForeground);
        }

        if (value instanceof ViewItem) {
            ViewItem item = (ViewItem) value;
            label.setText(item.toString());
            if (item.isLink()) {
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            }
            if (item.isHidden()) {
                setForeground(Color.gray);
            }

            if (item.isTopElement()) {
                iconHandler.findIconByName(ICON_UP, X16)
                        .or(() -> iconHandler.findIconByName(ICON_FOLDER, X16))
                        .ifPresent(label::setIcon);
            } else {
                getIcon(X16, item)
                        .ifPresent(label::setIcon);
            }
        }
        return label;
    }

    private Optional<Icon> getIcon(IconSize size, ViewItem item) {
        return Optional.ofNullable(item.getIconName())
                .flatMap(iconName -> iconHandler.findIconByName(item.getIconName(), size))
                .or(() -> iconHandler.findIconByFile(item.toFile(), size));
    }
}
