/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.ui.view.map;

import lombok.Getter;
import org.cosinus.streamer.gpx.GpxPoint;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;

import java.util.*;

import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;

public class MapModel extends ArrayList<GpxPoint> implements LoadWorkerModel<GpxPoint> {

    public static final int DEFAULT_PADDING = 10;

    @Getter
    protected final int padding;

    @Getter
    private final Set<Integer> selectedIndexes;

    private int currentIndex;

    @Getter
    protected double minX = MAX_VALUE;

    @Getter
    protected double maxX = MIN_VALUE;

    @Getter
    protected double minY = MAX_VALUE;

    @Getter
    protected double maxY = MIN_VALUE;

    @Getter
    protected Double scale;

    @Getter
    protected double middleX;

    @Getter
    protected double middleY;

    public MapModel() {
        this.selectedIndexes = new HashSet<>();
        this. padding = DEFAULT_PADDING;
    }

    public GpxPoint getCurrentItem() {
        return getItem(currentIndex).orElse(null);
    }

    public List<GpxPoint> getSelectedItems() {
        return range(0, size())
            .filter(selectedIndexes::contains)
            .mapToObj(this::get)
            .toList();
    }

    public String getCurrentItemIdentifier() {
        return getItem(currentIndex)
            .map(Object::toString)
            .orElse(null);
    }

    private Optional<GpxPoint> getItem(int itemInex) {
        return itemInex > 0 && itemInex < size() ? ofNullable(get(itemInex)) : empty();
    }

    @Override
    public void update(List<GpxPoint> points) {
        points.forEach(this::add);
    }

    @Override
    public boolean add(GpxPoint point) {
        boolean added = super.add(point);
        if (added) {
            minX = min(minX, point.getPoint().getLatitude().doubleValue());
            maxX = max(maxX, point.getPoint().getLatitude().doubleValue());
            minY = min(minY, point.getPoint().getLongitude().doubleValue());
            maxY = max(maxY, point.getPoint().getLongitude().doubleValue());
        }
        return added;
    }

    public void reset() {
        clear();
        selectedIndexes.clear();
        currentIndex = 0;
        scale = null;
    }

    public void initScale(int width, int height) {
        if (width > 0 && height > 0) {
            int size = min(width, height);
            int pad = 10;
            int drawable = size - pad * 2;

            scale = drawable / max(maxX - minX, maxY - minY);

            middleX = (drawable - (maxX - minX) * scale) / 2;
            middleY = (drawable - (maxY - minY) * scale) / 2;
        }
    }
}
