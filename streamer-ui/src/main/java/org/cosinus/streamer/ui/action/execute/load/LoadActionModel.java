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
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;

import static org.cosinus.streamer.ui.action.LoadStreamerAction.LOAD_STREAMER_ACTION_ID;

/**
 * Encapsulates the model of the load streamer action
 */
public class LoadActionModel<T> extends ActionModel {

    private final PanelLocation locationToLoadTo;

    private final Streamer<?> initialStreamerToLoad;

    private final String itemToSelectAfterLoad;

    private final String streamerViewNameToLoadIn;

    private final boolean expanding;

    private Streamer<T> streamerToLoad;

    private StreamerView<T, T> streamerViewToLoadTo;

    public LoadActionModel(final PanelLocation locationToLoadTo,
                           final Streamer<?> initialStreamerToLoad,
                           final String itemToSelectAfterLoad) {
        this(locationToLoadTo, initialStreamerToLoad, itemToSelectAfterLoad, null, true);
    }

    public LoadActionModel(final PanelLocation locationToLoadTo,
                           final Streamer<?> initialStreamerToLoad,
                           final String itemToSelectAfterLoad,
                           boolean expanding) {
        this(locationToLoadTo, initialStreamerToLoad, itemToSelectAfterLoad, null, expanding);
    }

    public LoadActionModel(final PanelLocation locationToLoadTo,
                           final Streamer<?> initialStreamerToLoad,
                           final String itemToSelectAfterLoad,
                           final String streamerViewNameToLoadIn) {
        this(locationToLoadTo, initialStreamerToLoad, itemToSelectAfterLoad, streamerViewNameToLoadIn, true);
    }

    public LoadActionModel(final PanelLocation locationToLoadTo,
                           final Streamer<?> initialStreamerToLoad,
                           final String itemToSelectAfterLoad,
                           final String streamerViewNameToLoadIn,
                           boolean expanding) {
        super(locationToLoadTo.name(), LOAD_STREAMER_ACTION_ID);
        this.locationToLoadTo = locationToLoadTo;
        this.initialStreamerToLoad = initialStreamerToLoad;
        this.itemToSelectAfterLoad = itemToSelectAfterLoad;
        this.streamerViewNameToLoadIn = streamerViewNameToLoadIn;
        this.expanding = expanding;
    }

    public PanelLocation getLocationToLoadTo() {
        return locationToLoadTo;
    }

    public Streamer<?> getInitialStreamerToLoad() {
        return initialStreamerToLoad;
    }

    public String getItemToSelectAfterLoad() {
        return itemToSelectAfterLoad;
    }

    public String getStreamerViewNameToLoadIn() {
        return streamerViewNameToLoadIn;
    }

    public boolean isExpanding() {
        return expanding;
    }

    public StreamerView<T, T> getStreamerViewToLoadTo() {
        return streamerViewToLoadTo;
    }

    public void setStreamerViewToLoadTo(StreamerView<T, T> streamerViewToLoadTo) {
        this.streamerViewToLoadTo = streamerViewToLoadTo;
    }

    public Streamer<T> getStreamerToLoad() {
        return streamerToLoad;
    }

    public void setStreamerToLoad(Streamer<T> streamerToLoad) {
        this.streamerToLoad = streamerToLoad;
    }
}
