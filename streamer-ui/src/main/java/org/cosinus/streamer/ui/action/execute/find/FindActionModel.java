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

import lombok.Builder;
import lombok.Getter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.UUID;
import java.util.function.Consumer;

import static org.cosinus.streamer.ui.action.FindStreamerAction.FIND_STREAMER_ACTION_ID;

@Getter
@Builder
public class FindActionModel implements ActionModel {

    private PanelLocation location;

    private boolean findingLastStreamer;

    private boolean falloutToParentStreamer;

    private boolean falloutToDefaultStreamer;

    private String streamerUrlToFind;

    private Consumer<Streamer<?>> streamerConsumer;

    public static FindActionModel findLastStreamerAndDo(final PanelLocation location,
                                                        final Consumer<Streamer<?>> consumer) {
        return FindActionModel.builder()
            .location(location)
            .findingLastStreamer(true)
            .falloutToParentStreamer(true)
            .falloutToDefaultStreamer(true)
            .streamerConsumer(consumer)
            .build();
    }

    public static FindActionModel findStreamerAndDo(final PanelLocation location,
                                                    String urlPath,
                                                    final Consumer<Streamer<?>> consumer) {
        return FindActionModel.builder()
            .location(location)
            .findingLastStreamer(false)
            .falloutToParentStreamer(false)
            .falloutToDefaultStreamer(false)
            .streamerUrlToFind(urlPath)
            .streamerConsumer(consumer)
            .build();
    }

    @Override
    public String getExecutionId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getActionId() {
        return FIND_STREAMER_ACTION_ID;
    }
}
