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

package org.cosinus.streamer.ui.view.geo;

import io.jenetics.jpx.WayPoint;
import org.cosinus.streamer.gpx.GpxPoint;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.form.ScrollPane;
import org.cosinus.swing.worker.WorkerListener;

import java.util.List;

import static java.awt.BorderLayout.CENTER;

public class MapStreamerView extends StreamerView<GpxPoint> {

    public static final String MAP_VIEWER = "map-viewer";

    public static final String STATUS_MAP_VIEW = "status-map-view";

    public static final String STATUS_MAP_VIEW_SELECTION = "status-map-view-selection";

    private MapModel mapModel;

    private MapView mapViewer;

    public MapStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        mapViewer = (MapView) viewer;
        mapViewer.initComponents();

        mapModel = mapViewer.getMapModel();

        ScrollPane scroll = new ScrollPane();
        scroll.setViewportView(mapViewer);
        streamerViewMainPanel.add(scroll, CENTER);

        initKeyActionsHandler();
        initCancelHandler();
        initFocusHandling();
    }

    @Override
    public String getStatus() {
        return mapModel.getSelectedIndexes().isEmpty() ?
            translator.translate(STATUS_MAP_VIEW, mapModel.size()) :
            translator.translate(STATUS_MAP_VIEW_SELECTION, mapModel.getSelectedIndexes().size(), mapModel.size());
    }

    @Override
    public String getName() {
        return MAP_VIEWER;
    }

    @Override
    public GpxPoint getCurrentItem() {
        return mapModel.getCurrentItem();
    }

    @Override
    public List<GpxPoint> getSelectedItems() {
        return mapModel.getSelectedItems();
    }

    @Override
    public LoadWorkerModel<GpxPoint> getLoadWorkerModel() {
        return mapModel;
    }

    @Override
    protected Viewer<GpxPoint> createViewer() {
        return new MapView();
    }
}
