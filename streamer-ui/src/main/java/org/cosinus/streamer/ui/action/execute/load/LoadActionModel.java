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

package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.LoadStreamerAction;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;

import static java.util.Optional.ofNullable;

/**
 * Encapsulates the model of the load streamer action
 */
public class LoadActionModel extends ActionModel {

    private final PanelLocation location;

    private final Streamer<?> streamerToLoad;

    private final String contentIdentifier;

    public LoadActionModel(StreamerView<?> view, Streamer<?> streamerToLoad) {
        this(view, streamerToLoad, ofNullable(view.getLoadedStreamer())
            .map(Streamer::getName)
            .orElse(null));
    }

    public LoadActionModel(StreamerView<?> view,
                           Streamer<?> streamerToLoad,
                           String contentIdentifier) {
        super(view.getId(), LoadStreamerAction.LOAD_STREAMER_ACTION_ID);
        this.location = view.getCurrentLocation();
        this.streamerToLoad = streamerToLoad;
        this.contentIdentifier = contentIdentifier;
    }

    public PanelLocation getLocation()
    {
        return location;
    }

    public Streamer<?>  getStreamerToLoad() {
        return streamerToLoad;
    }

    public String getContentIdentifier() {
        return contentIdentifier;
    }
}
