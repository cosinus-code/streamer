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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DetailHeader extends JTableHeader {

    private static final Logger LOG = LogManager.getLogger(DetailHeader.class);

    private int clickedColumn = -1;
    private int leftColIndex = -1;

    public DetailHeader(TableColumnModel model) {
        super(model);
    }

    @Override
    protected void processMouseEvent(MouseEvent evt) {
        switch (evt.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                if (!setMousePressed(evt)) {
                    return;
                }
                break;
            case MouseEvent.MOUSE_RELEASED:
                if (!setMouseReleased(evt)) {
                    return;
                }
                break;
            case MouseEvent.MOUSE_EXITED:
                setMouseMoved(evt,
                              false);
                break;
        }
        super.processMouseEvent(evt);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_MOVED) {
            setMouseMoved(evt, true);
        }
        super.processMouseMotionEvent(evt);
    }

    private boolean setMousePressed(MouseEvent evt) {
        table.requestFocus();
        if (evt.getButton() == MouseEvent.BUTTON3) {
            ((DetailTable) table).getPopupHeader().show(this,
                                                        evt.getX(),
                                                        evt.getY());
            return false;
        }

        TableColumnModel colModel = table.getColumnModel();

        clickedColumn = colModel.getColumnIndexAtX(evt.getX());
        if (clickedColumn == -1) {
            return true;
        }

        Rectangle headerRect = getHeaderRect(clickedColumn);
        if (clickedColumn == 0) {
            headerRect.width -= 3;
        } else {
            headerRect.grow(-3,
                            0);
        }

        if (!headerRect.contains(evt.getX(),
                                 evt.getY())) {
            leftColIndex = clickedColumn;
            if (evt.getX() < headerRect.x) {
                leftColIndex--;
            }
            if (leftColIndex < 0) {
                leftColIndex = 0;
            }
            clickedColumn = -1;
        } else {
            setHeaderCellPressed(true);
            getTable().getTableHeader().repaint(getTable().getTableHeader().getHeaderRect(clickedColumn));
        }

        return true;
    }

    private boolean setMouseReleased(MouseEvent evt) {
        try {
            if (clickedColumn >= 0 && evt.getButton() != MouseEvent.BUTTON3) {
                int oldSortedCol = ((DetailTable) table).getCurrentSortColumn();
                ((DetailTable) table).sort(clickedColumn);

                setHeaderCellPressed(false);
                getTable().getTableHeader().repaint(getTable().getTableHeader().getHeaderRect(clickedColumn));
                if (clickedColumn != oldSortedCol) {
                    getTable().getTableHeader().repaint(getTable().getTableHeader().getHeaderRect(oldSortedCol));
                }
            }
            leftColIndex = -1;
            return true;
        } catch (Exception ex) {
            LOG.error(ex);
            return false;
        }
    }

    public void setMouseMoved(MouseEvent evt,
                              boolean over) {
        int colOver = getOverColumn(evt, over);

        if (colOver > -1) {
            TableColumnModel model = table.getColumnModel();
            for (int i = 0; i < model.getColumnCount(); i++) {
                TableCellRenderer renderer = model.getColumn(i).getHeaderRenderer();
                ((DetailHeaderCell) renderer).setOver(i == colOver);
            }
        }
    }

    private int getOverColumn(MouseEvent evt,
                              boolean over) {
        if (over) {
            int colOver = table.getColumnModel().getColumnIndexAtX(evt.getX());
            if (colOver > -1) {
                Rectangle headerRectangle = getHeaderRect(colOver);
                if (headerRectangle.contains(evt.getX(),
                                             evt.getY())) {
                    return colOver;
                }
            }
        }

        return -1;
    }

    private void setHeaderCellPressed(boolean pressed) {
        TableCellRenderer renderer = table.getColumnModel().getColumn(clickedColumn).getHeaderRenderer();
        ((DetailHeaderCell) renderer).setPressed(pressed);
    }
}
