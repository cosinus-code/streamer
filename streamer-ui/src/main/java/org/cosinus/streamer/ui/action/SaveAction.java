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

import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.save.SaveActionModel;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_S;

@Component
//TODO: to eliminate T
public class SaveAction<T> extends StreamerAction<T> {
    public static final String SAVE_ACTION_ID = "save-streamer";

    private final ActionExecutors actionExecutors;

    private final ApplicationUIHandler uiHandler;

    public SaveAction(final ActionExecutors actionExecutors, final ApplicationUIHandler uiHandler) {
        this.actionExecutors = actionExecutors;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run(StreamerActionContext<T> context) {
        actionExecutors.execute(new SaveActionModel<T>(
            SAVE_ACTION_ID, context.getCurrentView().getLoadedStreamer(), context.getCurrentView()));
    }

    @Override
    public String getId() {
        return SAVE_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_S));
    }
}
