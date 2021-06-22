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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Table;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.MouseEvent.*;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.concat;
import static java.util.stream.IntStream.range;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.cosinus.streamer.ui.action.ExecuteStreamerAction.EXECUTE_STREAMER_ACTION_ID;

public abstract class DataTable extends Table implements FocusListener {

    public static final int FIND_STREAMER_SPEED = 500;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    @Autowired
    public ErrorHandler errorHandler;

    @Autowired
    public ActionController actionController;

    protected StreamerView<Streamer> view;

    //TODO
    protected boolean ctrlDown, shiftDown, altDown;

    private String nameToFind = "";

    private long lastActionTime;

    private int lastWidth, lastHeight;

    private DataTableModel model;

    @Override
    public void initComponents() {
        this.model = createDataTableModel();
        this.model.translate();

        setModel(model);

        setBorder(null);
        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        setRowMargin(0);
        getColumnModel().setColumnMargin(0);
        getTableHeader().setReorderingAllowed(false);

        getTableModel().setCurrentIndex(0);

        setFocusCycleRoot(true);
        setFocusTraversalKeysEnabled(false);
        addFocusListener(this);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(getKeyStroke(KeyEvent.VK_ENTER, 0), "no-action");
    }

    public void focusGained(FocusEvent e) {
        try {
            streamerViewHandler.setCurrentLocation(view.getCurrentLocation());
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
    }

    public void focusLost(FocusEvent e) {
    }

    public void init(StreamerView<Streamer> view) {
        this.view = view;
    }

//
//    @Override
//    public boolean requestFocusInWindow(){
//        boolean ret = super.requestFocusInWindow();
//        try{
//            Streamer dir = getCurrentFolder();
//            if(dir != null) getMainPanel().updateRoot(dir, panel);
//            updateStatus();
//        }
//        catch(Exception ex){
//            showError(ex);
//        }
//        return ret;
//    }

    @Override
    public void processMouseEvent(MouseEvent event) {
        try {
            if (event.getID() == MOUSE_RELEASED) {
//                Maestro.setDragged(false);
            } else if (event.getID() == MOUSE_EXITED) {
//                Maestro.setDragItself(false);
            } else if (event.getID() == MOUSE_ENTERED) {
//                Maestro.setDragItself(true);
            } else if (event.getID() == MOUSE_PRESSED) {
                int index = getIndexForItemAtPoint(event.getPoint());
                setCurrentIndex(index);
                requestFocus();
            } else if (event.getID() == MOUSE_CLICKED) {
                if (event.getButton() == BUTTON1) {
                    if (event.getClickCount() == 2) {
                        actionController.runAction(EXECUTE_STREAMER_ACTION_ID);
                    }
                } else if (event.getButton() == MouseEvent.BUTTON3) {
//                        Streamer streamer = getStreamerAt(index);
//                        JPopupMenu popup = Maestro.getMainFrame().getPopupMenuStreamer(streamer);
//                        if(popup != null) popup.show(jcTable.this, e.getX(), e.getY());
                }
            }
            super.processMouseEvent(event);
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent keyEvent) {
        // 33 = Page Up
        // 34 = Page Down
        // 35 = End
        // 36 = Home
        // 37 = Left arrow
        // 38 = Up arrow
        // 39 = Right arrow
        // 40 = Down arrow
        // 112 = F1
        // 123 = F12

        if (keyEvent.getID() == KEY_PRESSED) {
            shiftDown = keyEvent.getID() == KEY_PRESSED && keyEvent.isShiftDown();
            ctrlDown = keyEvent.getID() == KEY_PRESSED && keyEvent.isControlDown();
            altDown = keyEvent.getID() == KEY_PRESSED && keyEvent.isAltDown();

            actionController.runActionByKeyStroke(keyEvent);
            if (keyEvent.getKeyCode() >= ' ' &&
                keyEvent.getKeyCode() <= '~' &&
                !keyEvent.isAltDown() &&
                !keyEvent.isControlDown() &&
                !keyEvent.isShiftDown() &&
                !(keyEvent.getKeyCode() >= 33 && keyEvent.getKeyCode() <= 40) &&
                !(keyEvent.getKeyCode() >= 112 && keyEvent.getKeyCode() <= 123)) {

                if (!isAction(keyEvent.getWhen(), FIND_STREAMER_SPEED)) {
                    nameToFind = "";
                }
                nameToFind += (char) keyEvent.getKeyCode();
                movePositionByName(nameToFind);
                return;
            }
        }
        super.processComponentKeyEvent(keyEvent);
    }

    private boolean isAction(long actionTime, int speed) {
        boolean action = abs(this.lastActionTime - actionTime) < speed;
        lastActionTime = actionTime;
        return action;
    }

    public void selectCurrentStreamer() {
        selectStreamerAtIndex(getCurrentIndex());
        if (getCurrentIndex() != getStreamersCount() - 1) {
            setCurrentIndex(getCurrentIndex() + 1);
        }
    }

    public void selectStreamerAtIndex(int index) {
        getTableModel().addToSelection(index);
        repaint();
    }

    public void selectStreamers(int start, int end, boolean only, boolean deselect) {
        getTableModel().addToSelection(start, end, only, deselect);
        repaint();
    }

    public List<Streamer> getSelectedStreamers() {
        List<Streamer> selectedStreamers = getTableModel().getSelectedStreamers();
        return !selectedStreamers.isEmpty() ?
            selectedStreamers :
            stream(getSelectedRows())
                .filter(index -> index >= getTableModel().getMinimumToSelect())
                .mapToObj(this::getStreamerAt)
                .collect(toList());
    }

    public void movePositionByName(String name) {
        List<ViewItem> items = getAllItems();
        int min = model.isTopVisible() ? 1 : 0;
        int start = getSelectedRow() + (name.length() == 1 ? 1 : 0);

        concat(range(start, items.size()),
               range(min, start))
            .filter(i -> items.get(i).getName().toLowerCase().startsWith(name.toLowerCase()))
            .findFirst()
            .ifPresent(this::setCurrentIndex);
    }

    private int getIndexForItemAtPoint(Point point) {
        int row = rowAtPoint(point);
        int col = columnAtPoint(point);
        return getTableModel().getIndex(row,
                                        col);
    }

    public int getCurrentSortColumn() {
        return getTableModel().getCurrentSortColumn();
    }

    public boolean isSortAscending() {
        return getTableModel().isSortAscending();
    }

    public void sort(int col) {
        String name = getCurrentStreamerName();
        getTableModel().sort(col);
        findViewItem(name);
    }

    public String getCurrentStreamerName() {
        return Optional.ofNullable(getCurrentStreamer())
            .map(Streamer::getName)
            .orElse(null);
    }

    public Streamer getCurrentStreamer() {
        return getStreamerAt(getCurrentIndex());
    }

    public int getCurrentIndex() {
        return getTableModel().getCurrentIndex();
    }

    public Streamer getStreamerAt(int index) {
        if (index < 0 || index >= getStreamersCount()) {
            return null;
        }
        return getTableModel().getItemAt(index);
    }

    public int getStreamersCount() {
        return getTableModel().getItemsCount();
    }

    public List<ViewItem> getAllItems() {
        return getTableModel().getAllItems();
    }

    public boolean isIndexSelected(int index) {
        return getTableModel().isIndexSelected(index);
    }

    public void findViewItem(String name) {
        List<ViewItem> items = getAllItems();
        range(0, items.size())
            .filter(i -> name.equals(items.get(i).getName()))
            .findFirst()
            .ifPresent(this::setCurrentIndex);
    }

    public void updateForm() {
    }

    public void reset() {
        nameToFind = "";
        lastActionTime = 0;
    }

    public Streamer getCurrentFolder() {
        return getTableModel().getCurrentFolder();
    }

    public void onResize() {
        onResize(lastWidth, lastHeight);
    }

    public void onResize(int width, int height) {
        lastWidth = width;
        lastHeight = height;
    }

    protected DataTableModel getTableModel() {
        return (DataTableModel) getModel();
    }

    @Override
    public void translate() {
        getTableModel().translate();
    }

    public abstract void setCurrentIndex(int index);

    protected abstract DataTableModel createDataTableModel();
}
