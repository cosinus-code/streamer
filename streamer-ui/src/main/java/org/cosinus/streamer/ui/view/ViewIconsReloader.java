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

import org.cosinus.swing.image.icon.IconsResetEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Load again the file icons shown in the panels after the icons were reset, like after an icon theme change.
 * <p>
 * The file icons are not icon holders, so they are not updated with the icon holders:
 * without loading them again, they are not found in the emptied icons cache and shown empty.
 */
@Component
public class ViewIconsReloader {

    private final StreamerViewHandler streamerViewHandler;

    public ViewIconsReloader(final StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @EventListener(IconsResetEvent.class)
    public void reloadIcons() {
        streamerViewHandler.getPanels()
            .stream()
            .map(StreamerPanel::getView)
            // the event is published at startup too, before the panels show a view
            .filter(Objects::nonNull)
            .forEach(StreamerView::loadIcons);
    }
}
