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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.ui.view.ParentStreamerViewContext;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates the model of the delete streamers action
 */
public class DeleteActionModel<S extends Streamer<S>> extends ActionModel {

    private List<Streamer<S>> streamersToDelete = new ArrayList<>();

    private StreamerFilter streamerFilter;

    private ParentStreamer<S> parentStreamer;

    public DeleteActionModel(String actionId, String actionName) {
        super(UUID.randomUUID().toString(), actionId, actionName);
    }

    public static <S extends Streamer<S>> DeleteActionModel<S>
    delete(String actionId, String actionName, final ParentStreamerViewContext<S> from) {
        return new DeleteActionModel<S>(actionId, actionName)
            .deleteStreamers(from.getSelectedItems())
            .from(from.getParentStreamer());
    }

    public DeleteActionModel<S> deleteStreamers(List<Streamer<S>> streamersToDelete) {
        this.streamersToDelete = streamersToDelete;
        this.streamerFilter = streamersToDelete::contains;
        return this;
    }

    public StreamerFilter getStreamerFilter() {
        return streamerFilter;
    }

    public ParentStreamer<S> getParentStreamer() {
        return parentStreamer;
    }

    public DeleteActionModel<S> from(ParentStreamer<S> parentStreamer) {
        this.parentStreamer = parentStreamer;
        return this;
    }
}
