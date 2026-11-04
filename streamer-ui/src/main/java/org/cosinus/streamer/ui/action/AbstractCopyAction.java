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
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.streamer.api.transform.TransformBinaryStreamerHandler;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingActionWithModel;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.window.Dialog;

import java.util.List;

import static java.util.function.Predicate.not;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.BOUND;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Abstract copy action
 */
public abstract class AbstractCopyAction<M extends CopyActionModel> implements SwingActionWithModel<M> {

    private static final String COPY_CONFIRMATION_UI = "copyConfirmationDialog.json";

    protected final Preferences preferences;

    protected final Translator translator;

    protected final DialogHandler dialogHandler;

    protected final ActionExecutors actionExecutors;

    protected final LoadActionExecutor loadActionExecutor;

    protected final StreamerViewHandler streamerViewHandler;

    protected final TransformBinaryStreamerHandler transformBinaryStreamerHandler;

    protected final BinaryExpanderHandler binaryExpanderHandler;

    protected AbstractCopyAction(final Preferences preferences,
                                 final Translator translator,
                                 final DialogHandler dialogHandler,
                                 final ActionExecutors actionExecutors,
                                 final LoadActionExecutor loadActionExecutor,
                                 final StreamerViewHandler streamerViewHandler,
                                 final TransformBinaryStreamerHandler transformBinaryStreamerHandler,
                                 final BinaryExpanderHandler binaryExpanderHandler) {
        this.preferences = preferences;
        this.translator = translator;
        this.dialogHandler = dialogHandler;
        this.actionExecutors = actionExecutors;
        this.loadActionExecutor = loadActionExecutor;
        this.streamerViewHandler = streamerViewHandler;
        this.transformBinaryStreamerHandler = transformBinaryStreamerHandler;
        this.binaryExpanderHandler = binaryExpanderHandler;
    }

    @Override
    public void run(M copyActionModel) {
        if (preferences.booleanPreference(BOUND)) {
            return;
        }

        copyActionModel.targetTypes(targetTypes(copyActionModel.getStreamersToCopy()));
        dialogHandler.showDialog(() -> copyConfirmationDialog(copyActionModel))
            .response()
            .ifPresent(this::executeStreamerCopy);
    }

    public void executeStreamerCopy(final M copyAction) {
        actionExecutors.execute(copyAction);
    }

    /**
     * @param streamersToCopy the selected items to copy
     * @return the formats all the selected streamers can be transformed to, as they are transformed when copied;
     * the folders are not taken into account
     */
    protected List<String> targetTypes(final List<?> streamersToCopy) {
        List<Streamer<?>> streamer = streamersToCopy
            .stream()
            .filter(Streamer.class::isInstance)
            .filter(not(ParentStreamer.class::isInstance))
            .map(Streamer.class::cast)
            .<Streamer<?>>map(binaryExpanderHandler::expandStreamer)
            .toList();
        return transformBinaryStreamerHandler.targetFormats(streamer);
    }

    protected Dialog<M> copyConfirmationDialog(final M copyAction) {
        return dialogHandler.createDialog(applicationFrame, COPY_CONFIRMATION_UI, copyAction);
    }


    protected ParentStreamer<?> prepareDestination(M copyActionModel) {

        if (!copyActionModel.getDestination().getPath().equals(copyActionModel.getTargetPath())) {
            //TODO: to avoid cast
            ParentStreamer<?> destination =
                (ParentStreamer<?>) copyActionModel.getDestination().create(copyActionModel.getTargetPath(), true);
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
        return copyActionModel.getDestination();
    }
}
