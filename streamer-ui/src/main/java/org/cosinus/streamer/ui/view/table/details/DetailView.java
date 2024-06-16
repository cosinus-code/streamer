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

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.api.worker.DefaultWorkerListener;
import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.table.DataTable;
import org.cosinus.streamer.ui.view.table.TableStreamerView;

import java.awt.*;

import static java.util.Optional.ofNullable;

public class DetailView<T extends Streamable> extends TableStreamerView<T> {

    public static final String DETAIL_VIEW_NAME = "detail";

    private WorkerListener<? extends SaveWorkerModel<?>> saveListener;

    public DetailView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();
        this.saveListener = new DefaultWorkerListener<>() {
            @Override
            public void workerStarted(SaveWorkerModel<?> saveModel) {
                loadingIndicator.startLoading(saveModel.totalItemsToSave());
            }

            @Override
            public void workerUpdated(SaveWorkerModel<?> saveModel) {
                loadingIndicator.updateLoading(
                    saveModel.getSavedItemsCount(),
                    saveModel.totalItemsToSave());
            }

            @Override
            public void workerFinished(SaveWorkerModel<?> saveModel) {
                loadingIndicator.finishLoading();
                saveModel.setDirty(false);
                updateAddressBarAndStreamerPanel();
            }
        };

    }

    @Override
    public String getName() {
        return DETAIL_VIEW_NAME;
    }

    @Override
    protected DataTable<T> createDataTable() {
        return new DetailTable<>(this);
    }

    @Override
    protected DetailStreamerEditor<T> createStreamerEditor() {
        return new DetailStreamerEditor<>(this);
    }

    @Override
    public WorkerListener<? extends SaveWorkerModel<?>> getSaveListener() {
        return saveListener;
    }

    @Override
    public SaveWorkerModel<T> getSaveModel() {
        return (DetailStreamerEditor<T>) streamerEditor;
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
