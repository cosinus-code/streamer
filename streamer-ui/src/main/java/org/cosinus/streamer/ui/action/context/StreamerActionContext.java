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

package org.cosinus.streamer.ui.action.context;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.ActionContext;

/**
 * Streamer context for an action
 */
public class StreamerActionContext<T> implements ActionContext {

    private final Streamer<T> currentStreamer;

    private final StreamerView<T> currentView;

    private StreamerView<T> oppositeView;

    public StreamerActionContext(StreamerView<T> currentView) {
        this.currentView = currentView;
        this.currentStreamer = currentView.getLoadedStreamer();
    }

    public StreamerActionContext(Streamer<T> currentStreamer,
                                 StreamerView<T> currentView) {
        this.currentView = currentView;
        this.currentStreamer = currentStreamer;
    }

    public StreamerActionContext(StreamerView<T> currentView,
                                 StreamerView<T> oppositeView) {
        this.currentView = currentView;
        this.oppositeView = oppositeView;
        this.currentStreamer = (Streamer<T>) currentView.getCurrentContent();
    }

    public StreamerView<T>  getCurrentView() {
        return currentView;
    }

    public Streamer<T> getCurrentStreamer() {
        return currentStreamer;
    }

    public StreamerView<T> getOppositeView() {
        return oppositeView;
    }

    public PanelLocation getCurrentLocation() {
        return currentView.getCurrentLocation();
    }
}
