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

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Table;
import org.cosinus.swing.store.ApplicationStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.MouseEvent.*;
import static java.lang.Math.abs;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.concat;
import static java.util.stream.IntStream.range;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;
import static org.cosinus.streamer.ui.action.ExecuteStreamerAction.EXECUTE_STREAMER_ACTION_ID;

public abstract class DataTable<T extends Streamable> extends Table implements FocusListener {

    public static final int FIND_STREAMER_SPEED = 500;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected ActionController actionController;

    @Autowired
    protected ApplicationStorage applicationStorage;

    protected StreamerView<T, T> view;

    //TODO
    protected boolean ctrlDown, shiftDown, altDown;

    private String nameToFind = "";

    private long lastActionTime;

    private int lastWidth, lastHeight;

    protected DataTableModel<T> model;

    @Override
    public void initComponents() {
        this.model = createDataTableModel();

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

    @Override
    public void focusGained(FocusEvent e) {
        try {
            if (streamerViewHandler.getCurrentLocation() != view.getCurrentLocation()) {
                streamerViewHandler.setCurrentLocation(view.getCurrentLocation());
            }
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    public void init(StreamerView<T, T> view) {
        this.view = view;
    }

    @Override
    public void processMouseEvent(MouseEvent event) {
        try {
            if (event.getID() == MOUSE_RELEASED) {
//                setDragged(false);
            } else if (event.getID() == MOUSE_EXITED) {
//                setDragItself(false);
            } else if (event.getID() == MOUSE_ENTERED) {
//                setDragItself(true);
            } else if (event.getID() == MOUSE_PRESSED) {
                setCurrentIndex(getIndexForItemAtPoint(event.getPoint()));
                resetContentIdentifier();
            } else if (event.getID() == MOUSE_CLICKED) {
                if (isLeftMouseButton(event)) {
                    if (event.getClickCount() == 2) {
                        actionController.runAction(EXECUTE_STREAMER_ACTION_ID);
                    }
                } else if (isRightMouseButton(event)) {
                    //TODO: show context popup
                }
            }
            super.processMouseEvent(event);
        } catch (Exception ex) {
            errorHandler.handleError(this, ex);
        }
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getID() == KEY_PRESSED) {
            shiftDown = keyEvent.isShiftDown();
            ctrlDown = keyEvent.isControlDown();
            altDown = keyEvent.isAltDown();

            actionController.runActionByKeyStroke(keyEvent);
            if (actionController.isLetterKey(keyEvent)) {
                if (!isAction(keyEvent.getWhen(), FIND_STREAMER_SPEED)) {
                    nameToFind = "";
                }
                nameToFind += (char) keyEvent.getKeyCode();
                movePositionByName(nameToFind);
                resetContentIdentifier();
                return;
            }
        }
        super.processComponentKeyEvent(keyEvent);
        if (actionController.isGoKey(keyEvent)) {
            resetContentIdentifier();
        }
    }

    private boolean isAction(long actionTime, int speed) {
        boolean action = abs(this.lastActionTime - actionTime) < speed;
        lastActionTime = actionTime;
        return action;
    }

    public void selectCurrentItem() {
        if (getCurrentIndex() != getItemsCount() - 1) {
            selectCurrentIndex(getCurrentIndex() + 1);
        }
    }

    public List<T> getSelectedItems() {
        return stream(getSelectedRows())
            .filter(index -> index >= getTableModel().getMinimumToSelect())
            .mapToObj(this::getItemAt)
            .collect(toList());
    }

    public void movePositionByName(String name) {
        List<ViewItem> items = getAllItems();
        if (!items.isEmpty()) {
            int min = model.isTopVisible() ? 1 : 0;
            int start = getSelectedRow() + (name.length() == 1 ? 1 : 0);
            concat(range(start, items.size()),
                range(min, start))
                .filter(i -> i < items.size() && items.get(i).getName().toLowerCase().startsWith(name.toLowerCase()))
                .findFirst()
                .ifPresent(index -> setCurrentIndex(index, false));
        }
    }

    private int getIndexForItemAtPoint(Point point) {
        int row = rowAtPoint(point);
        int col = columnAtPoint(point);
        return getTableModel().getIndex(row,
            col);
    }

    public void sort(int column) {
        String name = getCurrentItemName();
        getTableModel().sort(column);
        findViewItem(name);

        int sortedColumn = getTableModel().getSortedColumn();
        boolean ascendingSorted = getTableModel().isSortAscending();
        applicationStorage.saveInt(sortingColumnKey(), sortedColumn);
        applicationStorage.saveBoolean(sortingAscendingKey(), ascendingSorted);
    }

    private String sortingColumnKey() {
        return storageKey("sorting", "column");
    }

    private String sortingAscendingKey() {
        return storageKey("sorting", "ascending");
    }

    protected String storageKey(String... key) {
        String location = view.getCurrentLocation().toString();
        String viewName = view.getName();
        String streamerKey = ofNullable(getParentStreamer())
            .map(Streamer::getProtocol)
            .orElse(null);

        return Stream.concat(Stream.of(location, viewName, streamerKey), stream(key))
            .filter(Objects::nonNull)
            .collect(Collectors.joining("|"));
    }

    public String getCurrentItemName() {
        return ofNullable(getCurrentItem())
            .map(Streamable::getName)
            .orElse(null);
    }

    public T getCurrentItem() {
        return getItemAt(getCurrentIndex());
    }

    public int getCurrentIndex() {
        return getTableModel().getCurrentIndex();
    }

    public T getItemAt(int index) {
        if (index < 0 || index >= getItemsCount()) {
            return null;
        }
        return getTableModel().getItemAt(index);
    }

    public int getItemsCount() {
        return getTableModel().getItemsCount();
    }

    public List<ViewItem> getAllItems() {
        return getTableModel().getAllViewItems();
    }

    public void findViewItem(String name) {
        List<ViewItem> items = getAllItems();
        range(0, items.size())
            .filter(i -> name.equals(items.get(i).getName()))
            .findFirst()
            .ifPresent(index -> setCurrentIndex(index, true));
    }

    public void updateForm() {
    }

    public void reset(final Streamer<T> parentStreamer) {
        nameToFind = "";
        lastActionTime = 0;
        model.reset(parentStreamer);

        int sortedColumn = applicationStorage.getInt(sortingColumnKey(), 0);
        boolean isAscendingSorted = applicationStorage.getBoolean(sortingAscendingKey(), false);
        model.setSortColumn(sortedColumn, isAscendingSorted);
    }

    public Streamer<T> getParentStreamer() {
        return view.getParentStreamer();
    }

    public void onResize() {
        onResize(lastWidth, lastHeight);
    }

    public void onResize(int width, int height) {
        lastWidth = width;
        lastHeight = height;
    }

    public DataTableModel<T> getTableModel() {
        return (DataTableModel<T>) getModel();
    }

    @Override
    public void translate() {
        getTableModel().translate();
    }

    public void setCurrentIndex(int index, boolean silent) {
        setCurrentIndex(index);
        if (!silent) {
            updateContentIdentifier();
        }
    }

    private void updateContentIdentifier() {
        getTableModel().setContentIdentifier(getCurrentItemName());
    }

    private void resetContentIdentifier() {
        getTableModel().setContentIdentifier(null);
    }

    public abstract void setCurrentIndex(int index);

    public abstract void selectCurrentIndex(int index);

    protected abstract DataTableModel<T> createDataTableModel();
}
