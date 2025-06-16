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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F7;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.dialog.OptionsDialog.PLAIN_MESSAGE;

/**
 * Rename streamer action
 */
@Component
public class CreateStreamerAction implements ActionInContext {

    public static final String CREATE_STREAMER_ACTION_ID = "create-streamer";

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final StreamerViewHandler streamerViewHandler;

    public CreateStreamerAction(final DialogHandler dialogHandler,
                                final Translator translator,
                                final StreamerViewHandler streamerViewHandler) {
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run(ActionContext context) {
        StreamerView<?, ?> currentView = streamerViewHandler.getCurrentView();
        if (currentView.getParentStreamer() instanceof ParentStreamer<?> parent) {
            if (!parent.canUpdate()) {
                return;
            }

            dialogHandler.showInputDialog(
                    applicationFrame,
                    translator.translate("act-new-enter-name"),
                    translator.translate("act-new-new-streamer"),
                    PLAIN_MESSAGE)
                .map(parent.getPath()::resolve)
                .map(path -> parent.create(path, true))
                .ifPresent(streamer -> {
                    streamer.save();
                    currentView.reload(streamer.getName());
                });
        }
    }

    @Override
    public String getId() {
        return CREATE_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_F7, 0));
    }
}
