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
package org.cosinus.streamer.ui.action.execute.save;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.UUID;

import static org.cosinus.streamer.ui.action.SaveAction.SAVE_ACTION_ID;

public class SaveActionModel<T> extends ActionModel {

    private final Streamer<T> streamerToSave;

    private final StreamerView<T> streamerView;

    public SaveActionModel(final Streamer<T> streamerToSave,
                           final StreamerView<T> streamerView) {
        super(UUID.randomUUID().toString(), SAVE_ACTION_ID);
        this.streamerToSave = streamerToSave;
        this.streamerView = streamerView;
    }

    public Streamer<T> getStreamerToSave() {
        return streamerToSave;
    }

    public StreamerView<T> getStreamerView() {
        return streamerView;
    }
}
