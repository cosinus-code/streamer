/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

/**
 * Zoom out the current view.
 * <p>
 * The shortcut is only set on the menu item (see menu.json), not as key stroke of the action,
 * so the key is not handled twice in a view which binds it itself (e.g. the text editor).
 */
@Component
public class ZoomOutAction implements SwingAction {

    public static final String ZOOM_OUT_ACTION_ID = "menu-view-zoom-out";

    private final StreamerViewHandler streamerViewHandler;

    public ZoomOutAction(final StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run() {
        ofNullable(streamerViewHandler.getCurrentView())
            .ifPresent(StreamerView::zoomOut);
    }

    @Override
    public String getId() {
        return ZOOM_OUT_ACTION_ID;
    }
}
