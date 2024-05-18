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

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;

/**
 * View item used in the view model
 */
public class ViewItem {

    private static final String TOP_ITEM_NAME = "..";

    private final boolean topItem;

    private final Streamable streamable;

    public ViewItem(Streamable streamable) {
        this(streamable, false);
    }

    public ViewItem(Streamable streamable, boolean topItem) {
        this.streamable = streamable;
        this.topItem = topItem;
    }

    public Streamable getStreamable() {
        return streamable;
    }

    public String getId() {
        return streamable.getId();
    }

    public String getName() {
        return streamable.getName();
    }

    public boolean isParent() {
        return streamable.isParent();
    }

    public boolean isLink() {
        return streamable.isLink();
    }

    public boolean isHidden() {
        return streamable.isHidden();
    }

    public boolean isTopItem() {
        return topItem;
    }

    public String getIconName() {
        return streamable.getIconName();
    }

    public Path getPath() {
        return streamable.getPath();
    }

    @Override
    public String toString() {
        return isTopItem() ? TOP_ITEM_NAME : streamable.getName();
    }

    public Value getDetail(int detailIndex) {
        return streamable.details().get(detailIndex);
    }

    public boolean isFile() {
        return streamable.isFile();
    }
}
