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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_R;

/**
 * Load streamer action
 */
@Component
public class ReloadStreamerAction implements ActionInContext {

    public static final String RELOAD_STREAMER_ACTION_ID = "menu-view-refresh";

    private final LoadActionExecutor loadActionExecutor;

    private final ApplicationUIHandler uiHandler;

    private final StreamerViewHandler streamerViewHandler;

    public ReloadStreamerAction(final LoadActionExecutor loadActionExecutor,
                                final ApplicationUIHandler uiHandler,
                                final StreamerViewHandler streamerViewHandler) {
        this.loadActionExecutor = loadActionExecutor;
        this.uiHandler = uiHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run(ActionContext context) {
        streamerViewHandler.getCurrentView().getParentStreamer().reset();
        loadActionExecutor.execute(new LoadActionModel(
            streamerViewHandler.getCurrentLocation(),
            streamerViewHandler.getCurrentView().getParentStreamer(),
            streamerViewHandler.getCurrentView().getCurrentItemIdentifier()));
    }

    @Override
    public String getId() {
        return RELOAD_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_R));
    }
}
