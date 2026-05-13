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

package org.cosinus.streamer.ui.view.tree;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static java.awt.Color.gray;
import static java.awt.Font.ITALIC;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.icon.IconSize.X16;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FOLDER;

public class TreeNodeRenderer extends DefaultTreeCellRenderer {

    public static final Border CELL_BORDER = emptyBorder(3, 3, 3, 3);

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private IconHandler iconHandler;

    @Autowired
    private FileHandler fileHandler;

    public TreeNodeRenderer() {
        injectContext(this);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        JLabel label = (JLabel) super
            .getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, false);

        label.setBorder(CELL_BORDER);
        if (value instanceof StreamerTreeNode node) {
            Streamer<?> streamer = node.getStreamer();

            label.setFont(streamer.isLink() ? tree.getFont().deriveFont(ITALIC) : tree.getFont());
            if (streamer.isHidden()) {
                label.setForeground(gray);
            }

            label.setText(streamer.getName());
            Optional<Icon> icon = getIcon(X16, streamer);
            icon.ifPresent(label::setIcon);
        }
        return label;
    }

    protected Optional<Icon> getIcon(final IconSize size, final Streamer<?> streamer) {
        return ofNullable(streamer.getIconName())
            .flatMap(iconName -> iconHandler.findIconByName(streamer.getIconName(), size, streamer.isIconRounded()))
            .or(() -> streamer.isFile() ?
                iconHandler.findIconByFile(createItemFile(streamer), size) :
                iconHandler.findIconByName(ICON_FOLDER, size, false)
                    .or(() -> uiHandler.getDefaultFileIcon()));
    }

    public File createItemFile(final Streamer<?> streamer) {
        return ofNullable(streamer.getRealPath())
            .map(Path::toFile)
            .filter(File::exists)
            .orElseGet(() -> fileHandler
                .createVirtualFile(streamer.getRealPath(), streamer.getName(), streamer.isParent()));
    }
}
