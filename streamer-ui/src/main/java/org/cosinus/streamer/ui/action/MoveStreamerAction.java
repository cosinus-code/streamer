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
import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.action.execute.move.MoveActionModel;
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

    public static final String MOVE_STREAMER_ACTION_ID = "act-move";

    public MoveStreamerAction(final Preferences preferences,
                              final Translator translator,
                              final DialogHandler dialogHandler,
                              final ActionExecutors actionExecutors,
                              final LoadActionExecutor loadActionExecutor,
                              final StreamerViewHandler streamerViewHandler) {
        super(preferences,
            translator,
            dialogHandler,
            actionExecutors,
            loadActionExecutor,
            streamerViewHandler);
    }

    @Override
    protected void executeStreamerCopy(MoveActionModel moveAction) {
        ParentStreamer<?> destination = prepareDestination(moveAction);
        moveAction.to(destination);
        super.executeStreamerCopy(moveAction);
    }

    @Override
    public MoveActionModel createActionModel() {
        final StreamerView<?, ?> sourceStreamerView = streamerViewHandler.getCurrentView();
        final StreamerView<?, ?> destinationStreamerView = streamerViewHandler.getOppositeView();

        return move()
            .streamers(sourceStreamerView.getSelectedItems())
            .from((ParentStreamer<?>) sourceStreamerView.getParentStreamer())
            .to((ParentStreamer<?>) destinationStreamerView.getParentStreamer());
    }

    @Override
    public String getId() {
        return MOVE_STREAMER_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_F6, 0));
    }
}
