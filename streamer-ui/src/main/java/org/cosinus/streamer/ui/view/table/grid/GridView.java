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

package org.cosinus.streamer.ui.view.table.grid;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.TableStreamerView;

import java.awt.*;

import static java.util.Optional.ofNullable;

public class GridView<T extends Streamable> extends TableStreamerView<T> {

    public static final String GRID_VIEW_NAME = "grid";

    private WorkerListener<SaveWorkerModel<T>, T> saveListener;

    public GridView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();
        this.saveListener = new WorkerListener<>() {
            @Override
            public void workerStarted(SaveWorkerModel<T> saveModel) {
                loadingIndicator.startLoading(saveModel.totalItemsToSave());
            }

            @Override
            public void workerUpdated(SaveWorkerModel<T> saveModel) {
                loadingIndicator.updateLoading(
                    saveModel.getSavedItemsCount(),
                    saveModel.totalItemsToSave());
            }

            @Override
            public void workerFinished(SaveWorkerModel<T> saveModel) {
                loadingIndicator.finishLoading();
                saveModel.setDirty(false);
                updateAddressBarAndStreamerPanel();
            }
        };

    }

    @Override
    public String getName() {
        return GRID_VIEW_NAME;
    }

    @Override
    protected DataTable<T> createDataTable() {
        return new GridTable<>(this);
    }

    @Override
    protected GridStreamerEditor<T> createStreamerEditor() {
        return new GridStreamerEditor<>(this);
    }

    @Override
    public WorkerListener<SaveWorkerModel<T>, T> getSaveListener() {
        return saveListener;
    }

    @Override
    public SaveWorkerModel<T> getSaveModel() {
        return (GridStreamerEditor<T>) streamerEditor;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || ofNullable(streamerEditor)
            .map(StreamerEditor::isDirty)
            .orElse(false);
    }

    public Rectangle getCurrentDetailRectangle(int detailIndex) {
        int index = table.getCurrentIndex();
        int row = table.getTableModel().getRowForIndex(index);
        int column = table.getTableModel().getColumnForIndex(index);

        Rectangle rect = table.getCellRect(row, detailIndex, true);
        int offsetIcon = column == 0 ? 21 : -2;
        return new Rectangle(rect.x + offsetIcon, rect.y, rect.width - offsetIcon + 4, rect.height);
    }

}
