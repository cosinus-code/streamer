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
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.view.DetailEditor;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.StreamerView;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static javax.swing.SwingUtilities.invokeLater;

public class DetailStreamerEditor<T extends Streamable> implements StreamerEditor<T>, SaveWorkerModel<T> {

    private final DetailView<T> view;

    private final List<DetailEditor<T>> detailEditors;

    private T itemToBeEdited;

    private long savedItemsCount;

    private boolean dirty;

    public DetailStreamerEditor(final DetailView<T> view) {
        this.view = view;
        this.detailEditors = IntStream.range(0, view.getParentStreamer().detailNames().size())
            .mapToObj(detailIndex -> new DetailEditor<>(this, detailIndex))
            .toList();
    }

    @Override
    public void loadAndShow(final T itemToBeEdited) {
        this.itemToBeEdited = itemToBeEdited;
        final AtomicBoolean focused = new AtomicBoolean();
        detailEditors.forEach(detailEditor -> {
            Rectangle detailRectangle = view.getCurrentDetailRectangle(detailEditor.getDetailIndex());
            if (detailRectangle != null) {
                detailEditor.setLoading(true);
                detailEditor.loadItem(itemToBeEdited);
                detailEditor.setBounds(detailRectangle);
                detailEditor.setVisible(true);
                if (detailEditor.isEnabled() && !focused.getAndSet(true)) {
                    detailEditor.requestFocus();
                }
                detailEditor.setLoading(false);
            }
        });
    }

    @Override
    public void save() {
        itemToBeEdited.save();
        setDirty(false);
        if (itemToBeEdited.getParent().isParent()) {
            view.reload(ofNullable(itemToBeEdited.details().get(itemToBeEdited.getLeadDetailIndex()))
                .map(Value::toString)
                .orElse(null));
        }
    }

    @Override
    public void setVisible(boolean visible) {
        detailEditors.forEach(detailEditor -> detailEditor.setVisible(visible));
        if (!visible) {
            invokeLater(view::requestFocus);
        }
    }

    @Override
    public StreamerView<T> getView() {
        return view;
    }

    @Override
    public Stream<T> streamToSave() {
        savedItemsCount = 0;
        return view.getDataTableModel().getAllItems();
    }

    @Override
    public StreamConsumer<T> streamConsumer() {
        return view.getParentStreamer().streamConsumer();
    }

    @Override
    public long totalItemsToSave() {
        return view.getDataTableModel().getAllItems().toList().size();
    }

    @Override
    public void update(List<T> items) {
        savedItemsCount += items.size();
    }

    public long getSavedItemsCount() {
        return savedItemsCount;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
