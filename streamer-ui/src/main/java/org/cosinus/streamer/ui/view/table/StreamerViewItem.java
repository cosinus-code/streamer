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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.swing.format.FormatHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * View item used in the view model
 */
public class StreamerViewItem implements ViewItem {

    private static final String TOP_ITEM_NAME = "..";

    @Autowired
    private FormatHandler formatHandler;
    private final boolean topItem;

    private final Streamer<?> streamer;

    public StreamerViewItem(Streamer<?> streamer) {
        this(streamer, false);
    }

    public StreamerViewItem(Streamer<?> streamer, boolean topItem) {
        injectContext(this);
        this.streamer = streamer;
        this.topItem = topItem;
    }

    public Streamer<?> getStreamer() {
        return streamer;
    }

    public String getName() {
        return streamer.getName();
    }

    @Override
    public String getValue() {
        return streamer.getValue();
    }

    @Override
    public String getType() {
        return streamer.getType();
    }

    @Override
    public String getDescription() {
        return streamer.getDescription();
    }

    @Override
    public long getSize() {
        return streamer.getSize();
    }

    @Override
    public long getLastModified() {
        return streamer.lastModified();
    }

    @Override
    public boolean isParent() {
        return streamer.isParent();
    }

    public boolean isLink() {
        return streamer.isLink();
    }

    public boolean isHidden() {
        return streamer.isHidden();
    }

    public boolean isTopItem() {
        return topItem;
    }

    public String getFormattedSize() {
        return streamer.isParent() ? "" : formatHandler.formatMemorySize(streamer.getSize());
    }

    public File toFile() {
        String fileName = ofNullable(streamer.getPath())
            .map(Objects::toString)
            .orElseGet(streamer::getName);
        return new File(fileName) {
            @Override
            public boolean isDirectory() {
                return streamer.isParent();
            }
        };
    }

    public String getIconName() {
        return streamer.getIconName();
    }

    @Override
    public String toString() {
        return isTopItem() ? TOP_ITEM_NAME : streamer.getName();
    }

    public Object getDetail(int column) {
        return ofNullable(streamer.detailNames())
            .filter(detailNames -> column < detailNames.size())
            .map(detailNames -> detailNames.get(column))
            .map(streamer.details()::get)
            .orElse(null);
    }
}
