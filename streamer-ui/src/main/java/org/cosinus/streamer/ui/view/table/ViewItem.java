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

package org.cosinus.streamer.ui.view.table;

import lombok.Getter;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.Objects;

/**
 * View item used in the view model
 */
@Getter
public class ViewItem {

    private static final String TOP_ITEM_NAME = "..";

    private final Streamable streamable;

    private final boolean topItem;

    private final String id;

    private final String name;

    private final boolean parent;

    private final boolean link;

    private final boolean hidden;

    private final String iconName;

    private final boolean iconRounded;

    private final Path path;

    private final String description;

    private final boolean file;

    public ViewItem(Streamable streamable) {
        this(streamable, false);
    }

    public ViewItem(Streamable streamable, boolean topItem) {
        this.streamable = streamable;
        this.topItem = topItem;
        this.id = streamable.getId();
        this.name = streamable.getName();
        this.parent = streamable.isParent();
        this.link = streamable.isLink();
        this.hidden = streamable.isHidden();
        this.iconName = streamable.getIconName();
        this.iconRounded = streamable.isIconRounded();
        this.path = streamable.getPath();
        this.description = streamable.getDescription();
        this.file = streamable.isFile();
    }

    public Value getDetail(int detailIndex) {
        return streamable.details().get(detailIndex);
    }

    @Override
    public String toString() {
        return isTopItem() ? TOP_ITEM_NAME : streamable.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ViewItem viewItem)) {
            return false;
        }
        return topItem == viewItem.topItem &&
            Objects.equals(getId(), viewItem.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(topItem, getId());
    }
}
