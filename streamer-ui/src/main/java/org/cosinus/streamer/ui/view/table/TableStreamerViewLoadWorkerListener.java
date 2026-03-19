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

package org.cosinus.streamer.ui.view.table;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewLoadWorkerListener;

import static java.util.Optional.ofNullable;

public class TableStreamerViewLoadWorkerListener<T extends Streamable, V> extends StreamerViewLoadWorkerListener<T, V> {

    protected final DataTable<T> table;

    public TableStreamerViewLoadWorkerListener(final TableStreamerView<T> streamerView) {
        super(streamerView);
        this.table = streamerView.table;
    }

    @Override
    public void workerStarted(LoadWorkerModel<V> loadWorkerModel) {
        table.getTableModel().fireContentChanged();
        super.workerStarted(loadWorkerModel);
    }

    @Override
    public void workerUpdated(LoadWorkerModel<V> loadWorkerModel) {
        table.getTableModel().fireContentChanged();
        super.workerUpdated(loadWorkerModel);
    }

    @Override
    public void workerFinished(LoadWorkerModel<V> loadWorkerModel) {
        super.workerFinished(loadWorkerModel);
        if (streamerView.isActive()) {
            ofNullable(loadWorkerModel.getContentIdentifier())
                .ifPresent(streamerView::findContent);
            if (table.getCurrentIndex() < 0) {
                table.setCurrentIndex(0, true);
            } else if (table.getCurrentIndex() >= table.getItemsCount()) {
                table.setCurrentIndex(table.getItemsCount() - 1, true);
            }
        }
    }
}
