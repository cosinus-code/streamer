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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.TableModel;
import org.cosinus.swing.preference.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SHOW_HIDDEN;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.TOP_VISIBLE;

public abstract class DataTableModel<T extends Streamable> extends TableModel implements LoadWorkerModel<T> {

    private static final Logger LOG = LogManager.getLogger(DataTableModel.class);

    protected final List<ViewItem> viewItems;

    private final ViewItemComparator comparator;

    protected final Map<Integer, Boolean> selectionMap;

    protected final Streamer<T> parentStreamer;

    protected final Map<String, T> streamableMap;

    protected String contentIdentifier;

    private int currentIndex;

    @Autowired
    public Preferences preferences;

    public DataTableModel(final Streamer<T> parentStreamer) {
        this.parentStreamer = parentStreamer;
        this.selectionMap = new ConcurrentHashMap<>();
        this.comparator = new ViewItemComparator();
        this.viewItems = new ArrayList<>();
        this.streamableMap = new HashMap<>();
    }

    @Override
    public String getContentIdentifier() {
        return contentIdentifier;
    }

    @Override
    public void setContentIdentifier(String contentIdentifier) {
        this.contentIdentifier = contentIdentifier;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public T getItemAt(int index) {
        return ofNullable(getViewItemAt(getRowForIndex(index), getColumnForIndex(index)))
            .map(ViewItem::getId)
            .map(streamableMap::get)
            .orElse(null);
    }

    private ViewItem getCurrentViewItem() {
        return getViewItemAt(getRowForIndex(currentIndex), getColumnForIndex(currentIndex));
    }

    private ViewItem getViewItemAt(int rowIndex, int columnIndex) {
        return (ViewItem) getValueAt(rowIndex, columnIndex);
    }

    public int getItemsCount() {
        return viewItems.size();
    }

    public List<ViewItem> getAllViewItems() {
        return viewItems;
    }

    public boolean isIndexSelected(int index) {
        if (index < 0 || index >= viewItems.size()) {
            return false;
        }
        return ofNullable(selectionMap.get(index))
            .orElse(false);
    }

    public boolean isSortAscending() {
        return comparator.isSortAscending();
    }

    public void sort(int column) {
        comparator.setSortType(column);
        sort();
    }

    public void sort() {
        try {
            viewItems.sort(comparator);
            fireTableDataChanged();
        } catch (Exception ex) {
            LOG.error("Error while sorting streamers", ex);
        }
    }

    public void addToSelection(int index) {
        if (index < getMinimumToSelect() || index >= viewItems.size()) {
            return;
        }
        selectionMap.put(index, ofNullable(selectionMap.get(index))
            .map(selected -> !selected)
            .orElse(true));
    }

    public void addToSelection(int start,
                               int end,
                               boolean only,
                               boolean deselect) {
        if (only) {
            clearSelection();
        }
        range(max(start, getMinimumToSelect()),
            min(viewItems.size(), end + 1))
            .forEach(i -> selectionMap.put(i, !deselect));
    }

    public void clearSelection() {
        selectionMap.clear();
    }

    public boolean isTopVisible() {
        return preferences.findBooleanPreference(TOP_VISIBLE)
            .orElse(true);
    }

    public int getMinimumToSelect() {
        return isTopVisible() ? 1 : 0;
    }

    public void clear() {
        viewItems.clear();
        selectionMap.clear();
        streamableMap.clear();
    }

    @Override
    public void init() {
        clear();
        if (isTopVisible()) {
            ofNullable(parentStreamer.getParent())
                .map(parent -> new ViewItem(parent, true))
                .ifPresent(this::addItem);
            fireTableDataChanged();
        }
    }

    @Override
    public void update(List<T> streamers) {
        boolean showHidden = preferences.booleanPreference(SHOW_HIDDEN);
        streamers
            .stream()
            .filter(Objects::nonNull)
            .filter(streamer -> !streamer.isHidden() || showHidden)
            .map(ViewItem::new)
            .forEach(this::addItem);

        viewItems.sort(comparator);
    }

    protected void addItem(ViewItem viewItem) {
        viewItems.add(viewItem);
        T streamer = (T) viewItem.getStreamable();
        streamableMap.put(viewItem.getId(), streamer);
    }

    public List<T> getSelectedItems() {
        return selectionMap.keySet()
            .stream()
            .map(viewItems::get)
            .map(ViewItem::getId)
            .map(streamableMap::get)
            .collect(Collectors.toList());
    }

    public String getCurrentItemIdentifier() {
        return ofNullable(getCurrentViewItem())
            .filter(not(ViewItem::isTopItem))
            .map(ViewItem::getName)
            .orElseGet(parentStreamer::getName);
    }

    public String getNextItemIdentifier() {
        int index = currentIndex < viewItems.size() - 1 ? currentIndex + 1 : currentIndex - 1;
        return index < 0 ? null :
            ofNullable(getViewItemAt(getRowForIndex(index), getColumnForIndex(index)))
                .filter(not(ViewItem::isTopItem))
                .map(ViewItem::getName)
                .orElseGet(parentStreamer::getName);
    }

    public abstract int getRowForIndex(int index);

    public abstract int getColumnForIndex(int index);

    public abstract int getIndex(int row, int column);

    public abstract void translate();
}
