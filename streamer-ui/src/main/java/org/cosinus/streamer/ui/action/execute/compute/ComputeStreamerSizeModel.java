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
package org.cosinus.streamer.ui.action.execute.compute;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.worker.WorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.cosinus.streamer.ui.action.ComputeStreamerSizeAction.COMPUTE_STREAMER_SIZE_ACTION_ID;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Model for computing a streamer size
 */
public class ComputeStreamerSizeModel extends ActionModel implements WorkerModel<Void> {

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    private final Streamer<?> streamer;

    public ComputeStreamerSizeModel(final Streamer<?> streamer) {
        super(streamer.getId(), COMPUTE_STREAMER_SIZE_ACTION_ID);
        injectContext(this);
        this.streamer = streamer;
    }

    public Streamer<?> getStreamer() {
        return streamer;
    }

    @Override
    public void update(List<Void> items) {
        streamerViewHandler.getCurrentView().repaint();
        streamerViewHandler.getOppositeView().repaint();
    }
}
