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

package org.cosinus.streamer.ui.view.table;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static java.awt.Color.gray;
import static java.awt.Font.ITALIC;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.view.table.icon.IconTable.PREVIEW_CELL_SIZE;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FOLDER;
import static org.cosinus.swing.image.icon.IconProvider.ICON_UP;

public abstract class TableCellRenderer<T extends DataTable> extends DefaultTableCellRenderer {

    private static final Logger LOG = LogManager.getLogger(TableCellRenderer.class);

    @Autowired
    protected Preferences preferences;

    @Autowired
    protected IconHandler iconHandler;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Autowired
    private FileHandler fileHandler;

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
            ofNullable(value).orElse(""),
            isSelected,
            hasFocus,
            row,
            column);

        if (component instanceof JLabel label && value instanceof ViewItem item) {
            if (item.isLink()) {
                label.setFont(label.getFont().deriveFont(ITALIC));
            }
            if (item.isHidden()) {
                label.setForeground(gray);
            }

            return getCellComponent(label,
                (T) table,
                item,
                isSelected,
                row,
                column);
        }

        return component;
    }

    protected abstract Component getCellComponent(JLabel label,
                                                  T table,
                                                  ViewItem value,
                                                  boolean isSelected,
                                                  int row,
                                                  int column);

    protected Optional<Icon> getUpIcon(IconSize size) {
        return iconHandler.findIconByName(ICON_UP, size, false)
            .or(() -> iconHandler.findIconByName(ICON_FOLDER, size, false));
    }

    protected Optional<Icon> getIcon(IconSize size, ViewItem item) {
        return getIcon(size, item, false);
    }

    protected Optional<Icon> getIcon(IconSize size, ViewItem item, boolean showPreview) {
        return ofNullable(item.getIconName())
            .flatMap(iconName -> iconHandler.findIconByName(item.getIconName(), size, item.isIconRounded()))
            .or(() -> item.isFile() ?
                showPreview ?
                    findIconWithPreview(size, createItemFile(item)) :
                    iconHandler.findIconByFile(createItemFile(item), size) :
                iconHandler.findIconByName(ICON_FOLDER, size, false)
                    .or(() -> uiHandler.getDefaultFileIcon()));
    }

    private Optional<Icon> findIconWithPreview(IconSize size, File itemFile) {
        return getPreviewIcon(itemFile)
            .or(() -> iconHandler.findIconByFile(itemFile, size));
    }

    private Optional<Icon> getPreviewIcon(File itemFile) {
        try {
            return iconHandler.getThumbnail(itemFile, PREVIEW_CELL_SIZE);
        } catch (IOException e) {
            LOG.error("Cannot create preview icon for item: " + itemFile);
            return Optional.empty();
        }
    }

    public File createItemFile(ViewItem item) {
        return ofNullable(item.getRealPath())
            .map(Path::toFile)
            .filter(File::exists)
            .orElseGet(() -> fileHandler.createVirtualFile(item.getRealPath(), item.getName(), item.isParent()));
    }

}
