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

package org.cosinus.streamer.ui.action.copy;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.LoadStreamerAction;
import org.cosinus.streamer.ui.action.StreamerAction;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.execute.DefaultWorkerListener;
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
import org.cosinus.streamer.ui.dialog.CopyConfirmationDialog;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;

import static org.cosinus.streamer.ui.preference.StreamerPreferences.BOUND;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Abstract stub of an copy action
 */
public abstract class AbstractCopyAction<A> extends StreamerAction<A> {

    protected final Preferences preferences;

    protected final Translator translator;

    protected final DialogHandler dialogHandler;

    protected final ActionExecutors actionExecutors;

    protected final WorkerListenerHandler workerListenerHandler;

    protected final LoadStreamerAction loadStreamerAction;

    protected AbstractCopyAction(Preferences preferences,
                                 Translator translator,
                                 DialogHandler dialogHandler,
                                 ActionExecutors actionExecutors,
                                 WorkerListenerHandler workerListenerHandler,
                                 LoadStreamerAction loadStreamerAction) {
        this.preferences = preferences;
        this.translator = translator;
        this.dialogHandler = dialogHandler;
        this.actionExecutors = actionExecutors;
        this.workerListenerHandler = workerListenerHandler;
        this.loadStreamerAction = loadStreamerAction;
    }

    @Override
    public void run(StreamerActionContext<A> actionContext) {

        if (preferences.booleanPreference(BOUND)) {
            return;
        }

        CopyActionModel copyAction = copySpecifications(actionContext);
        if (isEmpty(copyAction.getStreamersToCopy())) {
            return;
        }

        if (!copyAction.getSource().canRead() || !copyAction.getDestination().canWrite()) {
            dialogHandler.showInfo(translator.translate("act_copy_not_allowed"));
            return;
        }

        dialogHandler.showDialog(() -> copyConfirmationDialog(copyAction))
            .response()
            .ifPresent(action -> execute(action, actionContext));
    }

    protected <S extends Streamer<?>, T extends Streamer<?>> void execute(CopyActionModel<S, T> copyAction,
                                                                          StreamerActionContext actionContext) {
        workerListenerHandler.register(copyAction.getActionId(), new DefaultWorkerListener() {
            @Override
            public void workerFinished() {
                loadStreamerAction.run(new StreamerActionContext(actionContext.getOppositeView()));
            }
        });
        actionExecutors.execute(copyAction);
    }

    protected CopyConfirmationDialog copyConfirmationDialog(CopyActionModel copyAction) {
        return new CopyConfirmationDialog(copyAction);
    }

    protected abstract <S extends Streamer<?>, T extends Streamer<?>>
    CopyActionModel<S, T> copySpecifications(StreamerActionContext actionContext);
}
