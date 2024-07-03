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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.WorkerListenerHandler;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.dialog.CopyConfirmationDialog;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;

import static org.cosinus.streamer.ui.preference.StreamerPreferences.BOUND;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Abstract copy action
 */
public abstract class AbstractCopyAction implements ActionInContext {

    protected static final String COPY_ACTION_NAME = "act-copy";

    protected static final String MOVE_ACTION_NAME = "act-move";

    protected static final String PACK_ACTION_NAME = "act-pack";

    protected final Preferences preferences;

    protected final Translator translator;

    protected final DialogHandler dialogHandler;

    protected final ActionExecutors actionExecutors;

    protected final WorkerListenerHandler workerListenerHandler;

    protected final LoadActionExecutor loadActionExecutor;

    protected final StreamerViewHandler streamerViewHandler;

    protected AbstractCopyAction(final Preferences preferences,
                                 final Translator translator,
                                 final DialogHandler dialogHandler,
                                 final ActionExecutors actionExecutors,
                                 final WorkerListenerHandler workerListenerHandler,
                                 final LoadActionExecutor loadActionExecutor,
                                 final StreamerViewHandler streamerViewHandler) {
        this.preferences = preferences;
        this.translator = translator;
        this.dialogHandler = dialogHandler;
        this.actionExecutors = actionExecutors;
        this.workerListenerHandler = workerListenerHandler;
        this.loadActionExecutor = loadActionExecutor;
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run(ActionContext actionContext) {

        if (preferences.booleanPreference(BOUND)) {
            return;
        }

        Streamer<?> currentParentStreamer = streamerViewHandler.getCurrentView().getParentStreamer();
        Streamer<?> oppositeParentStreamer = streamerViewHandler.getOppositeView().getParentStreamer();
        if (currentParentStreamer.isParent() && oppositeParentStreamer.isParent()) {
            copyParentStreamer();
        }
    }

    protected <S extends Streamer<S>, T extends Streamer<T>> void copyParentStreamer() {
        CopyActionModel<S, T> copyAction = actionModel();
        if (isEmpty(copyAction.getStreamersToCopy())) {
            return;
        }

        if (!copyAction.getSource().canRead() || !copyAction.getDestination().canUpdate()) {
            dialogHandler.showInfo(translator.translate("act_copy_not_allowed"));
            return;
        }

        dialogHandler.showDialog(() -> copyConfirmationDialog(copyAction))
            .response()
            .ifPresent(this::executeStreamerCopy);
    }

    protected <S extends Streamer<S>, T extends Streamer<T>> void executeStreamerCopy(
        final CopyActionModel<S, T> copyAction) {
        actionExecutors.execute(copyAction);
    }

    protected <S extends Streamer<S>, T extends Streamer<T>> CopyConfirmationDialog copyConfirmationDialog(
        CopyActionModel<S, T> copyAction) {
        return new CopyConfirmationDialog(copyAction);
    }


    protected <S extends Streamer<S>, T extends Streamer<T>> ParentStreamer<T>
    prepareDestination(CopyActionModel<S, T> copyAction) {

        if (!copyAction.getDestination().getPath().equals(copyAction.getTargetPath())) {
            //TODO: to avoid cast
            ParentStreamer<T> destination =
                (ParentStreamer<T>) copyAction.getDestination().create(copyAction.getTargetPath(), true);
            if (destination == null) {
                return null;
            }
            if (!destination.exists()) {
                dialogHandler.showInfo(
                    applicationFrame, translator.translate("act_copy_destination_not_found", destination.getPath()));
                return null;
            }
            return destination;
        }
        return copyAction.getDestination();
    }

    protected abstract <S extends Streamer<S>, T extends Streamer<T>> CopyActionModel<S, T> actionModel();

    protected abstract String getCopyActionName();
}
