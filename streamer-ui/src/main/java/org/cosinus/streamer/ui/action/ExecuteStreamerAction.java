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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.util.Optional.ofNullable;

/**
 * Load streamer action
 */
@Component
public class ExecuteStreamerAction implements ActionInContext {

    public static final String EXECUTE_STREAMER_ACTION_ID = "execute-streamer";

    private final StreamerViewHandler streamerViewHandler;

    private final LoadActionExecutor loadActionExecutor;

    public ExecuteStreamerAction(final StreamerViewHandler streamerViewHandler,
                                 final LoadActionExecutor loadActionExecutor) {
        this.streamerViewHandler = streamerViewHandler;
        this.loadActionExecutor = loadActionExecutor;
    }

    @Override
    public void run(ActionContext context) {
        StreamerView<?> streamerView = streamerViewHandler.getCurrentView();
        ofNullable(streamerView.getCurrentItem())
            .filter(item -> Streamer.class.isAssignableFrom(item.getClass()))
            .map(Streamer.class::cast)
            .ifPresent(streamerToExecute -> {
                if (streamerToExecute.isParent()) {
                    loadActionExecutor.execute(new LoadActionModel(
                        streamerView.getCurrentLocation(),
                        streamerToExecute,
                        streamerView.getCurrentItemIdentifier(), false));
                    return;
                }

                streamerToExecute.getParent().execute(streamerToExecute.getPath());
            });
    }

    @Override
    public String getId() {
        return EXECUTE_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_ENTER, 0));
    }
}
