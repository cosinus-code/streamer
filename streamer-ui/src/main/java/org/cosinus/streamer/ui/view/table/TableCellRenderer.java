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

package org.cosinus.streamer.ui.view.table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.image.icon.IconSize;
import org.cosinus.swing.preference.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SELECT_BACKGROUND;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SELECT_FOREGROUND;
import static org.cosinus.streamer.ui.view.table.icon.IconTable.PREVIEW_CELL_SIZE;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FOLDER;
import static org.cosinus.swing.image.icon.IconProvider.ICON_UP;
import static org.cosinus.swing.image.icon.IconSize.X16;

public abstract class TableCellRenderer<T extends DataTable> extends DefaultTableCellRenderer {

    private static final Logger LOG = LogManager.getLogger(TableCellRenderer.class);

    @Autowired
    protected Preferences preferences;

    @Autowired
    protected IconHandler iconHandler;

    @Autowired
    private ImageHandler imageHandler;

    public TableCellRenderer() {
        injectContext(this);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Component component = super.getTableCellRendererComponent(table,
                                                                  value,
                                                                  isSelected,
                                                                  hasFocus,
                                                                  row,
                                                                  column);

        T dataTable = (T) table;
        int index = dataTable.getTableModel().getIndex(row, column);
        if (dataTable.isIndexSelected(index)) {
            preferences.findColorPreference(SELECT_BACKGROUND)
                .ifPresent(component::setBackground);
            preferences.findColorPreference(SELECT_FOREGROUND)
                .ifPresent(component::setForeground);
        }

        if (component instanceof JLabel label && value instanceof ViewItem item) {

            label.setText(item.toString());
            if (item.isLink()) {
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            }
            if (item.isHidden()) {
                label.setForeground(Color.gray);
            }

            return getCellComponent((JLabel) component,
                                    dataTable,
                                    item,
                                    isSelected,
                                    hasFocus,
                                    row,
                                    column);
        }

        return component;
    }

    protected abstract Component getCellComponent(JLabel label,
                                                  T table,
                                                  ViewItem value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row,
                                                  int column);

    protected Optional<Icon> getUpIcon() {
        return iconHandler.findIconByName(ICON_UP, X16)
            .or(() -> iconHandler.findIconByName(ICON_FOLDER, X16));
    }

    protected Optional<Icon> getIcon(IconSize size, ViewItem item) {
        return getIcon(size, item, false);
    }

    protected Optional<Icon> getIcon(IconSize size, ViewItem item, boolean showPreview) {
        return ofNullable(item.getIconName())
            .flatMap(iconName -> iconHandler.findIconByName(item.getIconName(), size))
            .or(() -> showPreview ?
                findIconWithPreview(size, item) :
                iconHandler.findIconByFile(item.toFile(), size));
    }

    private Optional<Icon> findIconWithPreview(IconSize size, ViewItem item) {
        return getPreviewIcon(item)
            .or(() -> iconHandler.findIconByFile(item.toFile(), size));
    }

    private Optional<Icon> getPreviewIcon(ViewItem item) {
        try {
            return imageHandler.getPreviewImage(item.toFile(), PREVIEW_CELL_SIZE);
        } catch (IOException e) {
            LOG.error("Cannot create preview icon for item: " + item);
            return Optional.empty();
        }
    }
}
