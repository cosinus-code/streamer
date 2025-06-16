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

import org.cosinus.streamer.ui.action.execute.save.SaveActionModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_S;

@Component
public class SaveAction implements ActionInContext {
    public static final String SAVE_ACTION_ID = "save-streamer";

    private final ActionExecutors actionExecutors;

    private final ApplicationUIHandler uiHandler;

    private final StreamerViewHandler streamerViewHandler;

    public SaveAction(final ActionExecutors actionExecutors,
                      final ApplicationUIHandler uiHandler,
                      final StreamerViewHandler streamerViewHandler) {
        this.actionExecutors = actionExecutors;
        this.uiHandler = uiHandler;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run(ActionContext context) {
        actionExecutors.execute(new SaveActionModel(
            streamerViewHandler.getCurrentView()));
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
