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
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F7;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.dialog.OptionsDialog.PLAIN_MESSAGE;

/**
 * Rename element action
 */
@Component
public class NewStreamerAction extends StreamerAction<Streamer<?>> {

    public static final String NEW_ELEMENT_ACTION_ID = "new-element";

    private final DialogHandler dialogHandler;

    private final Translator translator;

    public NewStreamerAction(DialogHandler dialogHandler,
                             Translator translator) {
        this.dialogHandler = dialogHandler;
        this.translator = translator;
    }

    @Override
    public void run(StreamerActionContext<Streamer<?>> context) {
        Streamer currentFolder = context.getCurrentView().getLoadedStreamer();
        if (!currentFolder.getParent().canWrite()) {
            return;
        }

        dialogHandler.showInputDialog(applicationFrame,
                                      translator.translate("act-new-enter-name"),
                                      translator.translate("act-new-new-element"),
                                      PLAIN_MESSAGE)
            .map(newName -> currentFolder.getPath().resolve(newName))
            .map(newPath -> currentFolder.getParent().createDirectoryStreamer(newPath))
            .map(Streamer::save)
            .ifPresent(createdElement -> context.getCurrentView().reload());
    }

    @Override
    public String getId() {
        return NEW_ELEMENT_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_F7, 0));
    }
}
