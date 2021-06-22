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
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_R;

/**
 * Load streamer action
 */
@Component
public class ReloadStreamerAction<T> extends StreamerAction<T> {

    public static final String RELOAD_STREAMER_ACTION_ID = "menu-view-refresh";

    private final ActionExecutors actionExecutors;

    private final ApplicationUIHandler uiHandler;

    public ReloadStreamerAction(ActionExecutors actionExecutors,
                                ApplicationUIHandler uiHandler) {
        this.actionExecutors = actionExecutors;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run(StreamerActionContext<T> context) {
        actionExecutors.execute(new LoadActionModel<>(
                context.getCurrentView(),
                context.getCurrentView().getLoadedStreamer(),
                context.getCurrentView().getSelectedContent()
                        .stream()
                        .filter(content -> Streamer.class.isAssignableFrom(content.getClass()))
                        .map(Streamer.class::cast)
                        .findFirst()
                        .orElse(null)));
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
