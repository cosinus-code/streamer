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
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel;
import org.cosinus.streamer.ui.action.execute.delete.DeleteStreamerExecutor;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_DELETE;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.dialog.OptionsDialog.YES_NO_CANCEL_OPTION;
import static org.cosinus.swing.image.icon.IconProvider.ICON_DELETE;

/**
 * Rename streamer action
 */
@Component
public class DeleteStreamerAction implements SwingAction {

    public static final String DELETE_STREAMER_ACTION_ID = "delete-streamer";

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final DeleteStreamerExecutor deleteExecutor;

    private final StreamerViewHandler streamerViewHandler;

    private final ApplicationUIHandler uiHandler;

    public DeleteStreamerAction(final DialogHandler dialogHandler,
                                final Translator translator,
                                final DeleteStreamerExecutor deleteExecutor,
                                final StreamerViewHandler streamerViewHandler,
                                final ApplicationUIHandler uiHandler) {
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.deleteExecutor = deleteExecutor;
        this.streamerViewHandler = streamerViewHandler;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        Streamer<?> currentParentStreamer = streamerViewHandler.getCurrentView().getParentStreamer();
        if (currentParentStreamer.isParent()) {
            deleteFromParentStreamer();
        }

    }

    private void deleteFromParentStreamer() {
        final StreamerView<Streamer<?>, ?> currentView =
            (StreamerView<Streamer<?>, ?>) streamerViewHandler.getCurrentView();

        if (currentView.getSelectedItems().isEmpty()) {
            return;
        }

        DeleteActionModel deleteActionModel = new DeleteActionModel()
            .deleteStreamers(currentView.getSelectedItems())
            .from((ParentStreamer<Streamer<?>>) currentView.getParentStreamer())
            .moveToTrash(moveToTrash());

        //TODO: to clarify streamer permissions
//        if (!deleteAction.getStreamer().canWriteTo(actionContext.getCurrentView().getLoadedStreamer())) {
//            dialogHandler.showInfo(translator.translate("act_copy_delete_not_allowed"));
//            return;
//        }

        if (!askForConfirmation() || dialogHandler.confirm(applicationFrame,
            translator.translate("act-delete-are-you-sure-streamers"),
            translator.translate(deleteActionModel.getActionId()),
            YES_NO_CANCEL_OPTION)) {
            deleteExecutor.execute(deleteActionModel);
        }
    }

    protected boolean askForConfirmation() {
        return true;
    }

    protected boolean moveToTrash() {
        return false;
    }

    @Override
    public String getIconName() {
        return ICON_DELETE;
    }

    @Override
    public String getId() {
        return DELETE_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_DELETE));
    }
}
