/*
 * IconCellRenderer.java
 *
 * Created on July 13, 2005, 2:11 PM
 */

package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.ui.view.table.TableCellRenderer;
import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.text.WrappedText;
import org.cosinus.swing.text.WrappedTextBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static org.cosinus.streamer.ui.preference.StreamerPreferences.PREVIEW;
import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * @author costaxus
 */
public class IconCellRenderer extends TableCellRenderer<IconTable> {

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
                                      boolean hasFocus,
                                      int row,
                                      int column) {
        int margin = 3;
        int maxWidth = table.getCellWidth() - 4 * margin;
        WrappedText wrappedText = new WrappedTextBuilder(maxWidth, label)
            .wrapOnSeparators(item.toString(), " .-_)(,")
            .toWrappedText(true);

        label.setText(wrappedText.getText());
        label.setOpaque(isSelected);
        label.setHorizontalAlignment(JLabel.CENTER);

        boolean showPreview = preferences.booleanPreference(PREVIEW);
        Optional<Icon> icon = item.isTopItem() ?
            getUpIcon() :
            getIcon(table.getIconSize(), item, showPreview);

        JLabel iconLabel = new JLabel(icon.orElse(null));
        int iconLabelSize = showPreview ? IconTable.PREVIEW_CELL_SIZE : table.getIconSize().getSize();
        iconLabel.setPreferredSize(new Dimension(iconLabelSize, iconLabelSize));

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        labelPanel.add(label);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(labelPanel, BorderLayout.NORTH);

        panel.setBorder(emptyBorder(margin));
        label.setBorder(emptyBorder(margin));

        labelPanel.setOpaque(false);
        textPanel.setOpaque(false);
        panel.setOpaque(false);

        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(textPanel, BorderLayout.CENTER);

        icon.map(Icon::getIconHeight)
            .map(iconHeight -> showPreview ? table.getIconDimension() : iconHeight)
            .map(iconHeight -> iconHeight + wrappedText.getHeight() + 4 * margin)
            .filter(panelHeight -> table.getRowHeight(row) < panelHeight)
            .ifPresent(panelHeight -> table.setRowHeight(row, panelHeight));

        return panel;
    }

}