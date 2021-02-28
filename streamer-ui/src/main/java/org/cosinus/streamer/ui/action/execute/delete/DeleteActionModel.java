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

package org.cosinus.streamer.ui.action.execute.delete;

import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Encapsulates the model of the delete elements action
 */
public class DeleteActionModel extends ActionModel {

    private List<Streamer> elementsToDelete = new ArrayList<>();

    private StreamerFilter streamerFilter;

    private DirectoryStreamer streamer;

    public DeleteActionModel(String actionName) {
        super(UUID.randomUUID().toString(), actionName);
    }

    public List<Streamer> getElementsToDelete() {
        return elementsToDelete;
    }

    public DeleteActionModel deleteElements(List<Streamer> elementsToDelete) {
        this.elementsToDelete = elementsToDelete;
        this.streamerFilter = elementsToDelete::contains;
        return this;
    }

    public StreamerFilter getStreamerFilter() {
        return streamerFilter;
    }

    public DirectoryStreamer getStreamer() {
        return streamer;
    }

    public DeleteActionModel from(DirectoryStreamer streamer) {
        this.streamer = streamer;
        return this;
    }

    public boolean hasElementsToDelete() {
        return !isEmpty(getElementsToDelete());
    }
}
