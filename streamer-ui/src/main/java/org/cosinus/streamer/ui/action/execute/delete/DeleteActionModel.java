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

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.cosinus.streamer.ui.action.DeleteStreamerAction.DELETE_STREAMER_ACTION_ID;

/**
 * Encapsulates the model of the delete streamers action
 */
@Getter
public class DeleteActionModel implements ActionModel {

    private final String executionId;

    private StreamerFilter streamerFilter;

    private ParentStreamer<Streamer<?>> from;

    private final boolean moveToTrash;

    public DeleteActionModel(final boolean moveToTrash) {
        this.executionId = randomUUID().toString();
        this.moveToTrash = moveToTrash;
    }

    public static DeleteActionModel delete() {
        return new DeleteActionModel(false);
    }

    public static DeleteActionModel moveToTrash() {
        return new DeleteActionModel(true);
    }

    public DeleteActionModel streamers(final List<Streamer<?>> streamersToDelete) {
        this.streamerFilter = streamersToDelete::contains;
        return this;
    }

    public DeleteActionModel from(ParentStreamer<Streamer<?>> parentStreamer) {
        this.from = parentStreamer;
        return this;
    }

    public ParentStreamer<Streamer<?>> source() {
        return from;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String getActionId() {
        return DELETE_STREAMER_ACTION_ID;
    }
}
