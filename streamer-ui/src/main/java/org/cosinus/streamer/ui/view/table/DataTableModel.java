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
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.TableModel;
import org.cosinus.swing.preference.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.SHOW_HIDDEN;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.TOP_VISIBLE;

public abstract class DataTableModel<T extends Streamer<?>> extends TableModel implements LoadWorkerModel<T>
{

    private static final Logger LOG = LogManager.getLogger(DataTableModel.class);

    protected final List<StreamerViewItem> viewItems;

    private final ViewItemComparator comparator;

    protected final Map<Integer, Boolean> selectionMap;

    protected Streamer<T> parentStreamer;

    protected String contentIdentifier;

    private int currentIndex;

    @Autowired
    public Preferences preferences;

    public DataTableModel() {
        this.selectionMap = new ConcurrentHashMap<>();
        this.comparator = new ViewItemComparator();
        this.viewItems = new ArrayList<>();
    }

    @Override
    public Streamer<T> getParentStreamer() {
        return parentStreamer;
    }

    @Override
    public void setParentStreamer(Streamer<T> parentStreamer)
    {
        this.parentStreamer = parentStreamer;
    }

    @Override
    public String getContentIdentifier()
    {
        return contentIdentifier;
    }

    @Override
    public void setContentIdentifier(String contentIdentifier)
    {
        this.contentIdentifier = contentIdentifier;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Streamer<?> getStreamerAt(int index) {
        return getViewItemAt(getRowForIndex(index), getColumnForIndex(index)).getStreamer();
    }

    private StreamerViewItem getViewItemAt(int rowIndex, int columnIndex) {
        return (StreamerViewItem) getValueAt(rowIndex, columnIndex);
    }

    public int getItemsCount() {
        return viewItems.size();
    }

    public List<StreamerViewItem> getAllViewItems() {
        return viewItems;
    }

    public boolean isIndexSelected(int index) {
        if (index < 0 || index >= viewItems.size()) {
            return false;
        }
        return ofNullable(selectionMap.get(index))
            .orElse(false);
    }

    public int getCurrentSortColumn() {
        return comparator.getCurrentSortColumn();
    }

    public boolean isSortAscending() {
        return comparator.isSortAscending();
    }

    public void sort(int col) {
        if (col < 0 || col >= getColumnCount()) {
            return;
        }
        comparator.setSortType(col);
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
    }

    @Override
    public void update(List<T> streamers)
    {
        if (viewItems.isEmpty() && isTopVisible()) {
            ofNullable(parentStreamer.getParent())
                .map(parent -> new StreamerViewItem(parent, true))
                .ifPresent(viewItems::add);
        }

        boolean showHidden = preferences.booleanPreference(SHOW_HIDDEN);
        streamers
            .stream()
            .filter(Objects::nonNull)
            .filter(streamer -> !streamer.isHidden() || showHidden)
            .map(StreamerViewItem::new)
            .forEach(viewItems::add);

        viewItems.sort(comparator);
    }

    public List<Streamer<?>> getSelectedStreamers() {
        return selectionMap.keySet()
            .stream()
            .map(viewItems::get)
            .map(StreamerViewItem::getStreamer)
            .collect(Collectors.toList());
    }

    public abstract int getRowForIndex(int index);

    public abstract int getColumnForIndex(int index);

    public abstract int getIndex(int row, int column);

    public abstract void translate();
}
