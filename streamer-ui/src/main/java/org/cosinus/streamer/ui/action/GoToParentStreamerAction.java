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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_UP;
import static java.util.Optional.ofNullable;

/**
 * Go to parent streamer action
 */
@Component
public class GoToParentStreamerAction implements ActionInContext {

    public static final String GO_TO_PARENT_ACTION = "menu-go-parent";

    private final ApplicationUIHandler uiHandler;

    private final LoadActionExecutor loadActionExecutor;

    private final StreamerViewHandler streamerViewHandler;

    public GoToParentStreamerAction(final ApplicationUIHandler uiHandler,
                                    final LoadActionExecutor loadActionExecutor,
                                    final StreamerViewHandler streamerViewHandler) {
        this.uiHandler = uiHandler;
        this.loadActionExecutor = loadActionExecutor;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run(ActionContext context) {
        StreamerView<?, ?> currentStreamerView = streamerViewHandler.getCurrentView();
        ofNullable(currentStreamerView.getParentStreamer())
            .map(Streamer::getParent)
            .map(parent -> new LoadActionModel(
                currentStreamerView.getCurrentLocation(),
                parent,
                currentStreamerView.getCurrentItemIdentifier()))
            .ifPresent(loadActionExecutor::execute);
    }

    @Override
    public String getId() {
        return GO_TO_PARENT_ACTION;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_UP));
    }
}
