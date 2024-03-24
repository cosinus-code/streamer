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

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;

import static java.awt.Cursor.E_RESIZE_CURSOR;
import static java.awt.event.MouseEvent.*;
import static java.util.stream.IntStream.range;

public class DetailHeader extends JTableHeader {

    private static final Logger LOG = LogManager.getLogger(DetailHeader.class);

    private int clickedColumn = -1;
    private int leftColIndex = -1;

    public DetailHeader(TableColumnModel model) {
        super(model);
    }

    @Override
    protected void processMouseEvent(MouseEvent event) {
        if (event.getID() == MOUSE_PRESSED) {
            if (!processMousePressed(event)) {
                return;
            }
        } else if (event.getID() == MOUSE_RELEASED) {
            if (!processMouseReleased(event)) {
                return;
            }
        } else if (event.getID() == MOUSE_EXITED) {
            processMouseOver(event, false);
        }
        super.processMouseEvent(event);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent event) {
        if (event.getID() == MOUSE_MOVED) {
            processMouseOver(event, true);
        }
        super.processMouseMotionEvent(event);
    }

    private boolean processMousePressed(MouseEvent event) {
        getTable().requestFocus();
        if (event.getButton() == BUTTON3) {
            getTable().getPopupHeader().show(this,
                                             event.getX(),
                                             event.getY());
            return false;
        }

        TableColumnModel colModel = getTable().getColumnModel();

        clickedColumn = colModel.getColumnIndexAtX(event.getX());
        if (clickedColumn == -1) {
            return true;
        }

        Rectangle headerRect = getHeaderRect(clickedColumn);
        if (clickedColumn == 0) {
            headerRect.width -= 3;
        } else {
            headerRect.grow(-3, 0);
        }

        if (!headerRect.contains(event.getX(), event.getY())) {
            leftColIndex = clickedColumn;
            if (event.getX() < headerRect.x) {
                leftColIndex--;
            }
            if (leftColIndex < 0) {
                leftColIndex = 0;
            }
            clickedColumn = -1;
        }

        return true;
    }

    private boolean processMouseReleased(MouseEvent event) {
        try {
            if (clickedColumn >= 0 && event.getButton() != BUTTON3) {
                int previousSortedColumn = getTable().getTableModel().getSortedColumn();
                getTable().sort(clickedColumn);
                repaint(getHeaderRect(clickedColumn));

                if (clickedColumn != previousSortedColumn) {
                    repaint(getHeaderRect(previousSortedColumn));
                }
            }
            leftColIndex = -1;
            return true;
        } catch (Exception ex) {
            LOG.error(ex);
            return false;
        }
    }

    public void processMouseOver(MouseEvent event, boolean over) {
        Optional<Integer> cellIndex = getColumnIndexForPoint(event.getX(), event.getY());

        Cursor cursor = cellIndex
            .filter(index -> over && isMouseOverCellRightMargin(index, event))
            .map(c -> E_RESIZE_CURSOR)
            .map(Cursor::getPredefinedCursor)
            .orElse(null);

        getTable().getView().setCursor(cursor);

        TableColumnModel model = getTable().getColumnModel();
        range(0, model.getColumnCount())
            .forEach(i -> {
                DetailHeaderCell c = (DetailHeaderCell) model.getColumn(i).getHeaderRenderer();
                boolean cellOver = over && cellIndex.map(index -> index == i).orElse(false);
                c.setOver(cellOver);
            });
    }

    private Optional<Integer> getColumnIndexForPoint(int x, int y) {
        return Optional.of(getTable().getColumnModel().getColumnIndexAtX(x))
            .filter(columnIndex -> columnIndex >= 0)
            .filter(columnIndex -> getHeaderRect(columnIndex).contains(x, y));
    }

    public boolean isMouseOverCellRightMargin(int index, MouseEvent event) {
        Rectangle cellRectangle = getHeaderRect(index);
        int marginSize = 4;
        return event.getX() > cellRectangle.getX() + cellRectangle.getWidth() - marginSize &&
            event.getX() < getWidth() - marginSize;
    }

    @Override
    public DetailTable<?> getTable() {
        return (DetailTable<?>) super.getTable();
    }
}
