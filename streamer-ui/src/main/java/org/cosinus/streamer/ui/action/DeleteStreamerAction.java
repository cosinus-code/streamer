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
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.WorkerModel;
import org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel;
import org.cosinus.streamer.ui.action.execute.DefaultWorkerListener;
import org.cosinus.streamer.ui.action.execute.WorkerListenerHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_DELETE;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.dialog.OptionsDialog.YES_NO_CANCEL_OPTION;

/**
 * Rename streamer action
 */
@Component
public class DeleteStreamerAction extends StreamerAction<Streamer<?>> {

    public static final String DELETE_STREAMER_ACTION_ID = "delete-streamer";

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final ActionExecutors actionExecutors;

    private final WorkerListenerHandler workerListenerHandler;

    private final LoadStreamerAction loadStreamerAction;

    public DeleteStreamerAction(DialogHandler dialogHandler,
                                Translator translator,
                                ActionExecutors actionExecutors,
                                WorkerListenerHandler workerListenerHandler,
                                LoadStreamerAction loadStreamerAction) {
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.actionExecutors = actionExecutors;
        this.workerListenerHandler = workerListenerHandler;
        this.loadStreamerAction = loadStreamerAction;
    }

    @Override
    public void run(StreamerActionContext<Streamer<?>> actionContext) {
        DeleteActionModel deleteAction = createDeleteActionModel(actionContext);
        if (!deleteAction.hasStreamersToDelete()) {
            return;
        }

//        //TODO: to clarify streamer permissions
//        if (!deleteAction.getStreamer().canWriteTo(actionContext.getCurrentView().getLoadedStreamer())) {
//            dialogHandler.showInfo(translator.translate("act_copy_delete_not_allowed"));
//            return;
//        }
//
//        if (deleteAction.getStreamersToDelete().size() == 1) {
//            Streamer streamerToDelete = deleteAction.getStreamersToDelete().get(0);
//            if (!streamerToDelete.isParent() && deleteApproved(streamerToDelete)) {
//                if (!actionContext.getCurrentStreamer().delete(streamerToDelete)) {
//                    dialogHandler.showInfo(translator.translate("act-delete-cannot", streamerToDelete));
//                    return;
//                }
//                actionContext.getCurrentView().reload();
//                return;
//            }
//        }

        if (dialogHandler.confirm(applicationFrame,
                                  translator.translate("act-delete-are-you-sure-streamers"),
                                  getActionName(),
                                  YES_NO_CANCEL_OPTION)) {
            workerListenerHandler.register(deleteAction.getActionId(), new DefaultWorkerListener() {

                @Override
                public void workerFinished(WorkerModel workerModel) {
                    loadStreamerAction.run(new StreamerActionContext(actionContext.getCurrentView()));
                }
            });
            actionExecutors.execute(deleteAction);
        }
    }

//    private boolean deleteApproved(Streamer streamerToDelete) {
//        return dialogHandler.confirm(translator.translate("act-delete-are-you-sure",
//                                                          streamerToDelete));
//    }

    private DeleteActionModel createDeleteActionModel(StreamerActionContext actionContext) {
        return new DeleteActionModel(getActionName())
            .deleteStreamers(new ArrayList<>(actionContext.getCurrentView().getSelectedContent()))
            //to avoid cast
            .from((ParentStreamer) actionContext.getCurrentView().getLoadedStreamer());
    }

    public String getActionName() {
        return translator.translate("act-delete");
    }

    @Override
    public String getId() {
        return DELETE_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_DELETE, 0));
    }
}
