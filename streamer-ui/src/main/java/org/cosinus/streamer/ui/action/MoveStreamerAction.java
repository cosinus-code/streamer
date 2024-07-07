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
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.move.MoveActionModel;
import org.cosinus.streamer.ui.view.ParentStreamerViewContext;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_F6;
import static org.cosinus.streamer.ui.action.execute.move.MoveActionModel.move;

/**
 * Copy streamers action
 */
@Component
public class MoveStreamerAction extends AbstractCopyAction<MoveActionModel> {

    public static final String MOVE_STREAMER_ACTION_ID = "move-streamer";

    public MoveStreamerAction(final Preferences preferences,
                              final Translator translator,
                              final DialogHandler dialogHandler,
                              final ActionExecutors actionExecutors,
                              final WorkerListenerHandler workerListenerHandler,
                              final LoadActionExecutor loadActionExecutor,
                              final StreamerViewHandler streamerViewHandler) {
        super(preferences,
            translator,
            dialogHandler,
            actionExecutors,
            workerListenerHandler,
            loadActionExecutor,
            streamerViewHandler);
    }

    @Override
    protected <S extends Streamer<S>, T extends Streamer<T>> void executeStreamerCopy(MoveActionModel moveAction) {
        ParentStreamer<T> destination = prepareDestination(moveAction);
        moveAction.to(destination);
        super.executeStreamerCopy(moveAction);
    }

    @Override
    protected <S extends Streamer<S>, T extends Streamer<T>> MoveActionModel<S, T> actionModel() {
        return move(getActionName(),
            new ParentStreamerViewContext<>((StreamerView<S>) streamerViewHandler.getCurrentView()),
            new ParentStreamerViewContext<>((StreamerView<T>) streamerViewHandler.getOppositeView()));
    }

    @Override
    public String getId() {
        return MOVE_STREAMER_ACTION_ID;
    }

    @Override
    protected String getActionName() {
        return MOVE_ACTION_NAME;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_F6, 0));
    }
}
