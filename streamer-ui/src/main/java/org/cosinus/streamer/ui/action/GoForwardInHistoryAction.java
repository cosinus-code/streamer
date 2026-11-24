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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerHistory;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_RIGHT;

/**
 * Go forward in the history of the streamers shown in the current panel.
 * <p>
 * Nothing is done if there is no next streamer in the history.
 */
@Component
public class GoForwardInHistoryAction extends GoInHistoryAction {

    public static final String GO_FORWARD_IN_HISTORY_ACTION = "menu-go-forward";

    public GoForwardInHistoryAction(final StreamerViewHandler streamerViewHandler,
                                    final StreamerHistory streamerHistory,
                                    final ActionExecutors actionExecutors) {
        super(streamerViewHandler, streamerHistory, actionExecutors);
    }

    @Override
    protected Optional<String> moveInHistory(PanelLocation location) {
        return streamerHistory.forward(location);
    }

    @Override
    public String getId() {
        return GO_FORWARD_IN_HISTORY_ACTION;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_RIGHT, ALT_DOWN_MASK));
    }
}
