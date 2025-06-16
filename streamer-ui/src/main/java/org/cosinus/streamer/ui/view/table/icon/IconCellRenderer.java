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

/*
 * IconCellRenderer.java
 *
 * Created on July 13, 2005, 2:11 PM
 */

package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.ui.view.table.TableCellRenderer;
import org.cosinus.streamer.ui.view.table.ViewItem;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static javax.swing.text.StyleConstants.*;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.PREVIEW;
import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * @author costaxus
 */
public class IconCellRenderer extends TableCellRenderer<IconTable> {

    private final IconTable table;

    public IconCellRenderer(final IconTable table) {
        this.table = table;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!(value instanceof ViewItem)) {
            component.setBackground(new Color(table.getBackground().getRGB()));
        }
        return component;
    }

    @Override
    public Component getCellComponent(JLabel label,
                                      IconTable table,
                                      ViewItem item,
                                      boolean isSelected,
                                      int row,
                                      int column) {

        Icon icon = getCellIcon(item);
        IconCellPanel cellPanel = new IconCellPanel(label, item, icon, table.getCellWidth(), isSelected);

        boolean showPreview = preferences.booleanPreference(PREVIEW);
        ofNullable(icon)
            .map(Icon::getIconHeight)
            .map(iconHeight -> showPreview ? table.getIconDimension() : iconHeight)
            .map(iconHeight -> iconHeight + cellPanel.getTextBound().height)
            .filter(panelHeight -> table.getRowHeight(row) < panelHeight)
            .ifPresent(panelHeight -> table.setRowHeight(row, panelHeight));

        return cellPanel;
    }

    private Icon getCellIcon(ViewItem item) {
        boolean showPreview = preferences.booleanPreference(PREVIEW);
        Optional<Icon> icon = item.isTopItem() ?
            getUpIcon(table.getIconSize()) :
            getIcon(table.getIconSize(), item, showPreview);
        return icon.orElse(null);
    }

    public JTextPane resetCellEditor(JTextPane cellEditor, int row, int column) {
        IconCellPanel iconCellPanel = (IconCellPanel) table.prepareRenderer(this, row, column);

        Font font = iconCellPanel.getTextFont();
        MutableAttributeSet standard = new SimpleAttributeSet();
        setAlignment(standard, ALIGN_CENTER);
        setFontFamily(standard, font.getFamily());
        setFontSize(standard, font.getSize());

        StyledDocument doc = cellEditor.getStyledDocument();
        doc.setParagraphAttributes(0, 0, standard, true);

        Rectangle cellRectangle = table.getCellRect(row, column, true);
        Rectangle textRectangle = iconCellPanel.getTextBound();
        int margin = textRectangle.x;
        cellEditor.setBounds(
            cellRectangle.x + textRectangle.x,
            cellRectangle.y + textRectangle.y,
            textRectangle.width + margin,
            textRectangle.height - 2 * margin);

        cellEditor.setBorder(emptyBorder(margin));
        cellEditor.setText(iconCellPanel.getWrappedText());

        return cellEditor;
    }
}