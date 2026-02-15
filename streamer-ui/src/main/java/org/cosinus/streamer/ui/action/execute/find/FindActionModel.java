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
package org.cosinus.streamer.ui.action.execute.find;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.UUID;
import java.util.function.Consumer;

import static org.cosinus.streamer.ui.action.FindStreamerAction.FIND_STREAMER_ACTION_ID;

public class FindActionModel implements ActionModel {

    private final String executionId;

    private final PanelLocation location;

    private final boolean findingLastStreamer;

    private final boolean falloutToParentStreamer;

    private final boolean falloutToDefaultStreamer;

    private final String streamerUrlToFind;

    private final Consumer<Streamer<?>> streamerConsumer;

    public FindActionModel(final PanelLocation location) {
        this(location, false, false, false, null, streamer -> {});
    }

    public FindActionModel(final PanelLocation location,
                           boolean findingLastStreamer,
                           boolean falloutToParentStreamer,
                           boolean falloutToDefaultStreamer,
                           final String streamerUrlToFind,
                           final Consumer<Streamer<?>> streamerConsumer) {
        this.executionId = UUID.randomUUID().toString();
        this.location = location;
        this.findingLastStreamer = findingLastStreamer;
        this.falloutToParentStreamer = falloutToParentStreamer;
        this.falloutToDefaultStreamer = falloutToDefaultStreamer;
        this.streamerUrlToFind = streamerUrlToFind;
        this.streamerConsumer = streamerConsumer;
    }

    public PanelLocation getLocation() {
        return location;
    }

    public boolean isFindingLastStreamer() {
        return findingLastStreamer;
    }

    public boolean isFalloutToParentStreamer() {
        return falloutToParentStreamer;
    }

    public boolean isFalloutToDefaultStreamer() {
        return falloutToDefaultStreamer;
    }

    public Consumer<Streamer<?>> streamerConsumer() {
        return streamerConsumer;
    }

    public String getStreamerUrlToFind() {
        return streamerUrlToFind;
    }

    public static FindActionModel findLastStreamerAndDo(final PanelLocation location,
                                                        final Consumer<Streamer<?>> consumer) {
        return new FindActionModel(location, true, true, true, null, consumer);
    }

    public static FindActionModel finaStreamerAndDo(final PanelLocation location,
                                                    String urlPath,
                                                    final Consumer<Streamer<?>> consumer) {
        return new FindActionModel(location, false, false, false, urlPath, consumer);
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public String getActionId() {
        return FIND_STREAMER_ACTION_ID;
    }
}
