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

import org.apache.commons.lang3.ObjectUtils;

import java.util.Comparator;

public class ViewItemComparator implements Comparator<ViewItem> {

    protected int column;

    private boolean ascending = true;

    public void setSortType(int column) {
        setSortType(column, this.column != column || !ascending);
    }

    public void setSortType(int column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public int getCurrentSortColumn() {
        return column;
    }

    public boolean isSortAscending() {
        return ascending;
    }

    @Override
    public int compare(ViewItem item1, ViewItem item2) {

        if (item1.isTopElement()) {
            return -1;
        }

        if (item2.isTopElement()) {
            return 1;
        }

        if (item1.getStreamer().isDirectory() && !item2.getStreamer().isDirectory()) {
            return -1;
        }

        if (!item1.getStreamer().isDirectory() && item2.getStreamer().isDirectory()) {
            return 1;
        }

        int compare;
        switch (column) {
            case 1:
                compare = ObjectUtils.compare(item1.getStreamer().getValue(),
                                              item2.getStreamer().getValue());
                break;
            case 2:
                compare = ObjectUtils.compare(item1.getStreamer().getType(),
                                              item2.getStreamer().getType());
                break;
            case 3:
                compare = Long.compare(item1.getStreamer().getSize(),
                                       item2.getStreamer().getSize());
                break;
            case 4:
                compare = Long.compare(item1.getStreamer().lastModified(),
                                       item2.getStreamer().lastModified());
                break;
            default:
                compare = ObjectUtils.compare(item1.getName(),
                                              item2.getName());
                break;
        }

        return ascending ? compare : -compare;
    }
}
