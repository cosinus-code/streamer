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
import org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel;
import org.cosinus.streamer.ui.action.execute.delete.DeleteStreamerExecutor;
import org.cosinus.streamer.ui.view.ParentStreamerViewContext;
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

import static java.awt.event.KeyEvent.VK_DELETE;
import static org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel.delete;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.dialog.OptionsDialog.YES_NO_CANCEL_OPTION;

/**
 * Rename streamer action
 */
@Component
public class DeleteStreamerAction implements ActionInContext {

    public static final String DELETE_STREAMER_ACTION_NAME = "delete-streamer";

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
    public void run(ActionContext context) {
        Streamer<?> currentParentStreamer = streamerViewHandler.getCurrentView().getParentStreamer();
        if (currentParentStreamer.isParent()) {
            deleteFromParentStreamer();
        }

    }

    private <S extends Streamer<S>> void deleteFromParentStreamer() {
        final StreamerView<S, S> currentView = (StreamerView<S, S>) streamerViewHandler.getCurrentView();

        ParentStreamerViewContext<S> streamerViewContext = new ParentStreamerViewContext<>(currentView);
        if (streamerViewContext.getSelectedItems().isEmpty()) {
            return;
        }

        DeleteActionModel<S> deleteAction = createDeleteActionModel(streamerViewContext);

          //TODO: to clarify streamer permissions
//        if (!deleteAction.getStreamer().canWriteTo(actionContext.getCurrentView().getLoadedStreamer())) {
//            dialogHandler.showInfo(translator.translate("act_copy_delete_not_allowed"));
//            return;
//        }

        if (dialogHandler.confirm(applicationFrame,
            translator.translate("act-delete-are-you-sure-streamers"),
            getActionName(),
            YES_NO_CANCEL_OPTION)) {
            deleteExecutor.execute(deleteAction);
        }
    }

    protected <S extends Streamer<S>> DeleteActionModel<S> createDeleteActionModel(
        final ParentStreamerViewContext<S> streamerViewContext) {
        return delete(getId(), getActionName(), streamerViewContext);
    }

    public String getActionName() {
        return DELETE_STREAMER_ACTION_NAME;
    }

    @Override
    public String getId() {
        return DELETE_STREAMER_ACTION_NAME;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_DELETE));
    }
}
