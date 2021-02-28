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
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;

/**
 * Encapsulates the model of the load element action
 */
public class LoadActionModel<T, S extends Streamer<T>> extends ActionModel {

    private final StreamerView<T> view;

    private final S streamer;

    private final Streamer contentToSelect;

    public LoadActionModel(StreamerView<T> view,
                           S streamer) {
        this(view, streamer, view.getLoadedStreamer());
    }

    public LoadActionModel(StreamerView<T> view,
                           S streamer,
                           Streamer contentToSelect) {
        super(view.getId(), LoadStreamerAction.LOAD_ELEMENT_ACTION_ID);
        this.view = view;
        this.streamer = streamer;
        this.contentToSelect = contentToSelect;
    }

    public StreamerView<T> getView() {
        return view;
    }

    public S getStreamer() {
        return streamer;
    }

    public Streamer getContentToSelect() {
        return contentToSelect;
    }
}
