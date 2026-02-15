/*
 * Copyright 2025 Cosinus Software
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

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.StreamerException;

import java.util.List;

public class ParentStreamerViewContext<T extends Streamer<T>> {

    @Getter
    private final ParentStreamer<T> parentStreamer;

    @Getter
    private final Streamer<T> currentItem;

    @Getter
    private final List<Streamer<T>> selectedItems;

    public ParentStreamerViewContext(final StreamerView<T, T> streamerView) {
        if (!streamerView.getParentStreamer().isParent()) {
            throw new StreamerException("Cannot create parent streamer context for a non parent streamer");
        }
        this.parentStreamer = (ParentStreamer<T>) streamerView.getParentStreamer();
        this.currentItem = streamerView.getCurrentItem();
        this.selectedItems = (List<Streamer<T>>) streamerView.getSelectedItems();
    }
}
