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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.ui.view.table.DataTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;

public class DetailHeaderCell extends JButton implements TableCellRenderer {

    private static final Logger LOG = LogManager.getLogger(DetailHeaderCell.class);

    protected boolean pressed, ascending, sorted, over;

    public DetailHeaderCell() {
        init();
    }

    public void init() {
        setHorizontalAlignment(SwingConstants.LEFT);
        setContentAreaFilled(false);
        setOpaque(true);
    }

    public void setOver(boolean over) {
        this.over = over;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintHeaderCellSeparator(g);
        if (sorted) {
            paintSortedSign(g);
        }
    }

    protected void paintHeaderCellSeparator(Graphics g) {
        g.drawLine(getWidth(), 0, getWidth(), getHeight());
    }

    protected void paintSortedSign(Graphics g) {
        int x = getWidth() - 10;
        int y = getHeight() / 2;
        int[] xxx, yyy;
        if (ascending) {
            xxx = new int[]{x, x - 3, x + 3};
            yyy = new int[]{y - 1, y + 2, y + 2};
        } else {
            xxx = new int[]{x - 3, x + 3, x};
            yyy = new int[]{y - 1, y - 1, y + 2};
        }
        Graphics2D g2d = (Graphics2D) g;
        Object oldRendering = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawPolygon(xxx, yyy, 3);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             oldRendering);
        g.fillPolygon(xxx, yyy, 3);
    }

    @Override
    public Component getTableCellRendererComponent(JTable jtable,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int rowIndex,
                                                   int colIndex) {
        try {
            DataTable table = (DataTable) jtable;
            if (table != null) {
                sorted = table.getCurrentSortColumn() == colIndex;
                ascending = table.isSortAscending();
            }
            customizeCellRenderer();

            ofNullable(value)
                .map(Object::toString)
                .map("  "::concat)
                .ifPresent(this::setText);
        } catch (Exception ex) {
            LOG.error("Cannot render table header cell", ex);
        }

        return this;
    }

    protected void customizeCellRenderer() {
        setBorder(emptyBorder(3));
    }
}
