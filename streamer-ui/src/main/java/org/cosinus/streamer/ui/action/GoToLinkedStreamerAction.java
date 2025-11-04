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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_L;
import static java.util.Optional.ofNullable;

@Component
public class GoToLinkedStreamerAction implements ActionInContext {

    public static final String GO_TO_LINKED_STREAMER_ACTION_NAME = "menu-go-to-linked-streamer";

    private final ApplicationUIHandler uiHandler;

    private final LoadActionExecutor loadActionExecutor;

    private final StreamerViewHandler streamerViewHandler;

    private final DialogHandler dialogHandler;

    private final Translator translator;

    public GoToLinkedStreamerAction(final ApplicationUIHandler uiHandler,
                                    final LoadActionExecutor loadActionExecutor,
                                    final StreamerViewHandler streamerViewHandler,
                                    final DialogHandler dialogHandler,
                                    final Translator translator) {
        this.uiHandler = uiHandler;
        this.loadActionExecutor = loadActionExecutor;
        this.streamerViewHandler = streamerViewHandler;
        this.dialogHandler = dialogHandler;
        this.translator = translator;
    }

    @Override
    public void run(ActionContext context) {
        StreamerView<?, ?> currentStreamerView = streamerViewHandler.getCurrentView();
        ofNullable(currentStreamerView.getCurrentItem())
            .filter(item -> Streamer.class.isAssignableFrom(item.getClass()))
            .map(Streamer.class::cast)
            .filter(Streamer::isLink)
            .map(Streamer::getLinkedStreamer)
            .map(linkedStreamer -> new LoadActionModel(
                currentStreamerView.getCurrentLocation(),
                linkedStreamer.getParent(),
                linkedStreamer.getName()))
            .ifPresentOrElse(loadActionExecutor::execute,
                () -> dialogHandler.showInfo(translator.translate("not-link")));
    }

    @Override
    public String getIconName() {
        return "insert-link";
    }

    @Override
    public String getId() {
        return GO_TO_LINKED_STREAMER_ACTION_NAME;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_L));
    }
}
