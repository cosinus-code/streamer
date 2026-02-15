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

import org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel;
import org.cosinus.streamer.ui.action.execute.delete.DeleteStreamerExecutor;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_DELETE;
import static org.cosinus.swing.image.icon.IconProvider.ICON_MOVE_TO_TRASH;

/**
 * Rename streamer action
 */
@Component
public class MoveToTrashStreamerAction extends DeleteStreamerAction {

    public static final String MOVE_TO_TRASH_STREAMER_ACTION_NAME = "move-to-trash-streamer";

    public MoveToTrashStreamerAction(final DialogHandler dialogHandler,
                                     final Translator translator,
                                     final DeleteStreamerExecutor deleteExecutor,
                                     final StreamerViewHandler streamerViewHandler,
                                     final ApplicationUIHandler uiHandler) {
        super(dialogHandler, translator, deleteExecutor, streamerViewHandler, uiHandler);

    }

    @Override
    protected boolean askForConfirmation() {
        return false;
    }

    @Override
    public DeleteActionModel createActionModel() {
        return super.createActionModel().moveToTrash();
    }

    @Override
    public String getIconName() {
        return ICON_MOVE_TO_TRASH;
    }

    @Override
    public String getId() {
        return MOVE_TO_TRASH_STREAMER_ACTION_NAME;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_DELETE, 0));
    }
}
