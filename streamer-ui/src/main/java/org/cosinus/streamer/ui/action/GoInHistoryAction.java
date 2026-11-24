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
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerHistory;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.action.execute.ActionExecutors;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.execute.find.FindActionModel.findStreamerAndDo;

/**
 * Base action moving in the history of the streamers shown in the current panel
 * and loading the streamer found there.
 * <p>
 * Nothing is done if there is no path to move to in the history.
 */
public abstract class GoInHistoryAction implements SwingAction {

    protected final StreamerViewHandler streamerViewHandler;

    protected final StreamerHistory streamerHistory;

    protected final ActionExecutors actionExecutors;

    protected GoInHistoryAction(final StreamerViewHandler streamerViewHandler,
                                final StreamerHistory streamerHistory,
                                final ActionExecutors actionExecutors) {
        this.streamerViewHandler = streamerViewHandler;
        this.streamerHistory = streamerHistory;
        this.actionExecutors = actionExecutors;
    }

    @Override
    public void run() {
        PanelLocation location = streamerViewHandler.getCurrentLocation();
        moveInHistory(location)
            .map(urlPath -> findStreamerAndDo(location, urlPath, streamer -> loadStreamer(location, streamer)))
            .ifPresent(actionExecutors::execute);
    }

    /**
     * Move in the history of the location.
     *
     * @param location the panel location
     * @return the path moved to, or empty if there is none
     */
    protected abstract Optional<String> moveInHistory(PanelLocation location);

    private void loadStreamer(PanelLocation location, Streamer<?> streamer) {
        ofNullable(streamer)
            .map(streamerToLoad -> new LoadActionModel<>(location, streamerToLoad, null))
            .ifPresent(actionExecutors::execute);
    }
}
