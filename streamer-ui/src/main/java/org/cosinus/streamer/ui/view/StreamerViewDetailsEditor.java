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
package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.value.Value;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static java.util.Optional.ofNullable;

public class StreamerViewDetailsEditor<T extends Streamable> {

    private final StreamerView<T> view;

    private List<DetailEditor<T>> detailEditors;

    private T itemToBeEdited;

    public StreamerViewDetailsEditor(final StreamerView<T> view) {
        this.view = view;
        this.detailEditors = IntStream.range(0, view.getParentStreamer().detailNames().size())
            .mapToObj(detailIndex -> new DetailEditor<>(this, detailIndex))
            .toList();
    }

    public void loadItemAndShow(final T itemToBeEdited) {
        this.itemToBeEdited = itemToBeEdited;
        final AtomicBoolean focused = new AtomicBoolean();
        detailEditors.forEach(detailEditor -> {
            Rectangle detailRectangle = view.getCurrentDetailRectangle(detailEditor.getDetailIndex());
            if (detailRectangle != null) {
                detailEditor.loadItem(itemToBeEdited);
                detailEditor.setBounds(detailRectangle);
                detailEditor.setVisible(true);
                if (detailEditor.isEnabled() && !focused.getAndSet(true)) {
                    detailEditor.requestFocus();
                }
            }
        });
    }


    public void saveItem() {
        itemToBeEdited.save();
        view.reload(ofNullable(itemToBeEdited.details().get(itemToBeEdited.getLeadDetailIndex()))
            .map(Value::toString)
            .orElse(null));
    }

    public void hideEditor() {
        setVisible(false);
        SwingUtilities.invokeLater(view::requestFocus);
    }

    public void setVisible(boolean visible) {
        detailEditors.forEach(detailEditor -> detailEditor.setVisible(visible));
    }

    public StreamerView<T> getView() {
        return view;
    }
}
