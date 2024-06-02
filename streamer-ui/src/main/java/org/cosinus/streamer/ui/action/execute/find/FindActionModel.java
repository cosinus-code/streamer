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
package org.cosinus.streamer.ui.action.execute.find;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.UUID;
import java.util.function.Consumer;

import static org.cosinus.streamer.ui.action.FindStreamerAction.FIND_STREAMER_ACTION_ID;

public class FindActionModel extends ActionModel {

    private final PanelLocation location;

    private final boolean findingLastStreamer;

    private final Consumer<Streamer<?>> streamerConsumer;

    public FindActionModel(final PanelLocation location) {
        this(location, false, streamer -> {});
    }

    public FindActionModel(final PanelLocation location,
                           boolean findingLastStreamer,
                           final Consumer<Streamer<?>> streamerConsumer) {
        super(UUID.randomUUID().toString(), FIND_STREAMER_ACTION_ID);
        this.location = location;
        this.findingLastStreamer = findingLastStreamer;
        this.streamerConsumer = streamerConsumer;
    }

    public PanelLocation getLocation() {
        return location;
    }

    public boolean isFindingLastStreamer() {
        return findingLastStreamer;
    }

    public Consumer<Streamer<?>> streamerConsumer() {
        return streamerConsumer;
    }

    public static FindActionModel findLastStreamerAndConsume(final PanelLocation location,
                                                             final Consumer<Streamer<?>> consumer) {
        return new FindActionModel(location, true, consumer);
    }
}
