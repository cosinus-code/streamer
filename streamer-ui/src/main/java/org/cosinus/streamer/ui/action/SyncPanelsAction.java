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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_T;

@Component
public class SyncPanelsAction implements SwingAction {

    public static final String SYNC_PANELS_ACTION_ID = "sync-panels";

    private final ApplicationUIHandler uiHandler;

    private final StreamerViewHandler streamerViewHandler;

    private final LoadActionExecutor loadActionExecutor;

    public SyncPanelsAction(final ApplicationUIHandler uiHandler,
                            final StreamerViewHandler streamerViewHandler,
                            final LoadActionExecutor loadActionExecutor) {
        this.uiHandler = uiHandler;
        this.streamerViewHandler = streamerViewHandler;
        this.loadActionExecutor = loadActionExecutor;
    }

    @Override
    public void run() {
        loadActionExecutor.execute(new LoadActionModel(
            streamerViewHandler.getCurrentView().getCurrentLocation(),
            streamerViewHandler.getOppositeView().getParentStreamer(),
            streamerViewHandler.getOppositeView().getCurrentItemIdentifier()));
    }

    @Override
    public String getId() {
        return SYNC_PANELS_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_T));
    }
}
