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
import org.cosinus.streamer.ui.action.execute.compute.ComputeStreamerSizeModel;
import org.cosinus.streamer.ui.action.execute.compute.ComputeStreamerSizeExecutor;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_SPACE;
import static java.util.Optional.ofNullable;

/**
 * Compute streamer size action
 */
@Component
public class ComputeStreamerSizeAction implements ActionInContext {

    public static final String COMPUTE_STREAMER_SIZE_ACTION_ID = "compute-streamer-size";

    private final StreamerViewHandler streamerViewHandler;

    private final ComputeStreamerSizeExecutor computeStreamerSizeExecutor;

    public ComputeStreamerSizeAction(final StreamerViewHandler streamerViewHandler, ComputeStreamerSizeExecutor computeStreamerSizeExecutor) {
        this.streamerViewHandler = streamerViewHandler;
        this.computeStreamerSizeExecutor = computeStreamerSizeExecutor;
    }

    @Override
    public void run(ActionContext context) {
        ofNullable(streamerViewHandler.getCurrentView().getCurrentStreamer())
            .ifPresent(streamer -> {
                ComputeStreamerSizeModel computeStreamerSizeModel = new ComputeStreamerSizeModel(streamer);
                computeStreamerSizeModel.registerListeners(
                    streamerViewHandler.getCurrentView(),
                    streamerViewHandler.getOppositeView());

                computeStreamerSizeExecutor.execute(computeStreamerSizeModel);
                streamerViewHandler.getCurrentView().goNext();
            });
    }

    @Override
    public String getId() {
        return COMPUTE_STREAMER_SIZE_ACTION_ID;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_SPACE, 0));
    }
}
