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

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.action.execute.delete.DeleteActionModel;
import org.cosinus.streamer.ui.action.progress.DefaultProgressListener;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F8;
import static org.cosinus.swing.dialog.OptionsDialog.YES_NO_CANCEL_OPTION;

/**
 * Rename element action
 */
@Component
public class DeleteStreamerAction extends StreamerAction<Streamer<?>> {

    public static final String DELETE_ELEMENT_ACTION_ID = "delete-element";

    public final SwingApplicationContext swingContext;

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final SwingApplicationFrame applicationFrame;

    private final ActionExecutors actionExecutors;

    private final ProgressListenerHandler progressListenerHandler;

    private final LoadStreamerAction loadStreamerAction;

    public DeleteStreamerAction(SwingApplicationContext swingContext,
                                DialogHandler dialogHandler,
                                Translator translator,
                                SwingApplicationFrame applicationFrame,
                                ActionExecutors actionExecutors,
                                ProgressListenerHandler progressListenerHandler,
                                LoadStreamerAction loadStreamerAction) {
        this.swingContext = swingContext;
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.applicationFrame = applicationFrame;
        this.actionExecutors = actionExecutors;
        this.progressListenerHandler = progressListenerHandler;
        this.loadStreamerAction = loadStreamerAction;
    }

    @Override
    public void run(StreamerActionContext<Streamer<?>> actionContext) {
        DeleteActionModel deleteAction = createDeleteActionModel(actionContext);
        if (!deleteAction.hasElementsToDelete()) {
            return;
        }

//        //TODO: to clarify streamer permissions
//        if (!deleteAction.getStreamer().canWriteTo(actionContext.getCurrentView().getLoadedStreamer())) {
//            dialogHandler.showInfo(translator.translate("act_copy_delete_not_allowed"));
//            return;
//        }
//
//        if (deleteAction.getElementsToDelete().size() == 1) {
//            Element elementToDelete = (Element) deleteAction.getElementsToDelete().get(0);
//            if (!elementToDelete.isDirectory() && deleteApproved(elementToDelete)) {
//                if (!actionContext.getCurrentStreamer().delete(elementToDelete)) {
//                    dialogHandler.showInfo(translator.translate("act-delete-cannot", elementToDelete));
//                    return;
//                }
//                actionContext.getCurrentView().reload();
//                return;
//            }
//        }

        if (dialogHandler.confirm(applicationFrame,
                                  translator.translate("act-delete-are-you-sure-elements"),
                                  getActionName(),
                                  YES_NO_CANCEL_OPTION)) {
            progressListenerHandler.register(new DefaultProgressListener() {
                @Override
                public void finishProgress() {
                    loadStreamerAction.run(new StreamerActionContext(actionContext.getCurrentView()));
                }
            });
            actionExecutors.execute(deleteAction);
        }
    }

//    private boolean deleteApproved(Element elementToDelete) {
//        return dialogHandler.confirm(translator.translate("act-delete-are-you-sure",
//                                                          elementToDelete));
//    }

    private DeleteActionModel createDeleteActionModel(StreamerActionContext actionContext) {
        return new DeleteActionModel(getActionName())
                .deleteElements(new ArrayList<>(actionContext.getCurrentView().getSelectedContent()))
                //to avoid cast
                .from((DirectoryStreamer) actionContext.getCurrentView().getLoadedStreamer());
    }

    public String getActionName() {
        return translator.translate("act-delete");
    }

    @Override
    public String getId() {
        return DELETE_ELEMENT_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_F8, 0));
    }
}
