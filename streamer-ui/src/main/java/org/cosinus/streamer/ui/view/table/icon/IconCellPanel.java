package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.ui.view.table.ViewItem;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.image.icon.IconSize;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.text.WrappedText;
import org.cosinus.swing.text.TextWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;

import static org.cosinus.streamer.ui.preference.StreamerPreferences.ICON_SIZE;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.PREVIEW;
import static org.cosinus.streamer.ui.view.table.icon.IconTable.PREVIEW_CELL_SIZE;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.image.icon.IconSize.X32;

public class IconCellPanel extends Panel {

    private final int margin = 3;

    @Autowired
    protected Preferences preferences;

    private final WrappedText wrappedText;

    private final WrappedText wrappedTextHML;

    private final Font textFont;

    private final int cellWidth;

    public IconCellPanel(JLabel textLabel, ViewItem item, Icon icon, int cellWidth, boolean isSelected) {
        this.textFont = textLabel.getFont();
        this.cellWidth = cellWidth;

        setLayout(new BorderLayout());
        setBorder(emptyBorder(margin));
        setOpaque(false);

        int maxWidth = cellWidth - 4 * margin;
        TextWrapper textWrapper = new TextWrapper(maxWidth, textLabel)
            .wrapOnSeparators(item.toString(), " .-_)(,");

        wrappedText = textWrapper.toWrappedText(false);
        wrappedTextHML = textWrapper.toWrappedText(true);

        textLabel.setText(wrappedTextHML.text());
        textLabel.setOpaque(isSelected);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setBorder(emptyBorder(margin));

        JLabel iconLabel = new JLabel(icon);
        int iconLabelSize = getIconSize();
        iconLabel.setPreferredSize(new Dimension(iconLabelSize, iconLabelSize));

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        textPanel.setOpaque(false);
        textPanel.add(textLabel);

        JPanel subIconPanel = new JPanel(new BorderLayout());
        subIconPanel.setOpaque(false);
        subIconPanel.add(textPanel, BorderLayout.NORTH);

        add(iconLabel, BorderLayout.NORTH);
        add(subIconPanel, BorderLayout.CENTER);
    }

    public String getWrappedText() {
        return wrappedText.text();
    }

    public int getIconSize() {
        return preferences.booleanPreference(PREVIEW) ?
            PREVIEW_CELL_SIZE :
            preferences.findStringPreference(ICON_SIZE)
                .flatMap(IconSize::forText)
                .orElse(X32)
                .getSize();
    }

    public Font getTextFont() {
        return textFont;
    }

    public int getMargin() {
        return margin;
    }

    public Rectangle getTextBound() {
        return new Rectangle(margin, getIconSize() + margin,
            cellWidth - 4 * margin, wrappedTextHML.height() - 2 * margin);
    }
}
