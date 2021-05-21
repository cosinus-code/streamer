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

import org.cosinus.streamer.api.Element;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.LoadStreamerAction;
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
import java.util.stream.IntStream;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.MouseEvent.*;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.concat;
import static java.util.stream.IntStream.range;
import static javax.swing.KeyStroke.getKeyStroke;

public abstract class DataTable extends Table implements FocusListener {

    public static final int DOUBLE_CLICK_SPEED = 300;

    public static final int FIND_ELEMENT_SPEED = 500;

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
//            Element dir = getCurrentFolder();
//            if(dir != null) getMainPanel().updateRoot(dir, panel);
//            updateStatus();
//        }
//        catch(Exception ex){
//            showError(ex);
//        }
//        return ret;
//    }

    @Override
    public void processMouseEvent(MouseEvent e) {
        try {
            if (e.getID() == MOUSE_RELEASED) {
//                Maestro.setDragged(false);
            } else if (e.getID() == MOUSE_EXITED) {
//                Maestro.setDragItself(false);
            } else if (e.getID() == MOUSE_ENTERED) {
//                Maestro.setDragItself(true);
            } else if (e.getID() == MOUSE_PRESSED) {
                requestFocus();
            } else if (e.getID() == MOUSE_CLICKED) {
                int index = getIndexForElementAtPoint(e.getPoint());
                Element element = getElementAt(index);
                if (element != null) {
                    if (e.getButton() == BUTTON1) {
                        if (index == getCurrentIndex()) {
                            if (isAction(e.getWhen(), DOUBLE_CLICK_SPEED)) {
                                actionController.runAction(LoadStreamerAction.LOAD_ELEMENT_ACTION_ID);
                            }
                            return;
                        }
                    }
//                    else if(e.getButton() == MouseEvent.BUTTON3){
//                        requestFocus();
//                        setCurrentIndex(index);
//                        JPopupMenu popup = Maestro.getMainFrame().getPopupMenuElement(element);
//                        if(popup != null) popup.show(jcTable.this, e.getX(), e.getY());
//                    }
                }
            }
            super.processMouseEvent(e);
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

                if (!isAction(keyEvent.getWhen(), FIND_ELEMENT_SPEED)) {
                    nameToFind = "";
                }
                nameToFind += (char) keyEvent.getKeyCode();
                movePositionByName(nameToFind);
                return;
            }
        }
        super.processComponentKeyEvent(keyEvent);
    }

    private boolean isAction(long actionTime,
                             int speed) {
        boolean action = abs(this.lastActionTime - actionTime) < speed;
        lastActionTime = actionTime;
        return action;
    }

    public void selectCurrentStreamer() {
        selectElement(getCurrentIndex());
        if (getCurrentIndex() != getStreamersCount() - 1) {
            setCurrentIndex(getCurrentIndex() + 1);
        }
    }

    public void selectElement(int index) {
        getTableModel().addToSelection(index);
        repaint();
    }

    public void selectElements(int start,
                               int end,
                               boolean only,
                               boolean deselect) {
        selectElements(start,
                       end,
                       only,
                       deselect,
                       true);
    }

    public void selectElements(int start,
                               int end,
                               boolean only,
                               boolean deselect,
                               boolean repaint) {
        getTableModel().addToSelection(start,
                                       end,
                                       only,
                                       deselect);
        if (repaint) {
            repaint();
        }
    }

    public List<Streamer> getSelectedStreamers() {
        List<Streamer> selectedElements = getTableModel().getSelectedElements();
        return !selectedElements.isEmpty() ?
            selectedElements :
            stream(getSelectedRows())
                .filter(index -> index >= getTableModel().getMinimumToSelect())
                .mapToObj(this::getElementAt)
                .collect(toList());
    }

    public void movePositionByName(String name) {
        List<ViewItem> items = getAllElements();
        int min = model.isTopVisible() ? 1 : 0;
        int start = getSelectedRow() + (name.length() == 1 ? 1 : 0);

        concat(range(start, items.size()),
               range(min, start))
            .filter(i -> items.get(i).getName().toLowerCase().startsWith(name.toLowerCase()))
            .findFirst()
            .ifPresent(this::setCurrentIndex);
    }

    private int getIndexForElementAtPoint(Point point) {
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
        String name = getCurrentElementName();
        getTableModel().sort(col);
        findElement(name);
    }

    public String getCurrentElementName() {
        return Optional.ofNullable(getCurrentStreamer())
            .map(Element::getName)
            .orElse(null);
    }

    public Streamer getCurrentStreamer() {
        return getElementAt(getCurrentIndex());
    }

    public int getCurrentIndex() {
        return getTableModel().getCurrentIndex();
    }

    public Streamer getElementAt(int index) {
        if (index < 0 || index >= getStreamersCount()) {
            return null;
        }
        return getTableModel().getElementAt(index);
    }

    public int getStreamersCount() {
        return getTableModel().getElementCount();
    }

    public List<ViewItem> getAllElements() {
        return getTableModel().getAllElements();
    }

    public boolean isIndexSelected(int index) {
        return getTableModel().isIndexSelected(index);
    }

    public void findElement(String name) {
        List<ViewItem> items = getAllElements();
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
