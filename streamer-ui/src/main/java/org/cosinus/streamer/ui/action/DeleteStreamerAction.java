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
import org.cosinus.swing.action.SwingActionWithModel;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_DELETE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel.delete;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.dialog.OptionsDialog.YES_NO_CANCEL_OPTION;
import static org.cosinus.swing.image.icon.IconProvider.ICON_DELETE;

/**
 * Rename streamer action
 */
@Component
public class DeleteStreamerAction implements SwingActionWithModel<DeleteActionModel> {

    public static final String DELETE_STREAMER_ACTION_ID = "delete-streamer";

    protected final DialogHandler dialogHandler;

    protected final Translator translator;

    protected final DeleteStreamerExecutor deleteExecutor;

    protected final StreamerViewHandler streamerViewHandler;

    protected final ApplicationUIHandler uiHandler;

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
    public void run(final DeleteActionModel deleteActionModel) {
        if (!askForConfirmation() || dialogHandler.confirm(applicationFrame,
            translator.translate("act-delete-are-you-sure-streamers"),
            translator.translate(getId()),
            YES_NO_CANCEL_OPTION)) {
            deleteExecutor.execute(deleteActionModel);
        }
    }

    @Override
    public DeleteActionModel createActionModel() {
        final StreamerView<?> currentView = streamerViewHandler.getCurrentView();
        if (!isEmpty(currentView.getSelectedItems()) &&
            currentView.getParentStreamer().isParent()) {

            return delete()
                //TODO: to find a way to avoid cast (a dedicated StreamerView for Streamer<?>)
                .streamers((List<Streamer<?>>) currentView.getSelectedItems())
                .from((ParentStreamer<Streamer<?>>) currentView.getParentStreamer());
        }

        return null;
    }

    protected boolean askForConfirmation() {
        return true;
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
