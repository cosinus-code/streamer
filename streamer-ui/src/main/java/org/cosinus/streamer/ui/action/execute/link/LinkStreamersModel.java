/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.ui.action.execute.link;

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.List;

import static java.util.UUID.randomUUID;

@Getter
public class LinkStreamersModel implements ActionModel {

    public static final String LINK_STREAMER_ACTION_ID = "link-streamer-action-id";

    private List<Streamer<?>> streamersToLink;

    private StreamerView<?> sourceView;

    private ParentStreamer<?> source;

    private StreamerView<?> destinationView;

    private ParentStreamer<?> destination;

    public static LinkStreamersModel link() {
        return new LinkStreamersModel();
    }

    public LinkStreamersModel streamers(final List<Streamer<?>> streamersToLink) {
        this.streamersToLink = streamersToLink;
        return this;
    }

    public LinkStreamersModel fromView(StreamerView<?> sourceView) {
        this.sourceView = sourceView;
        //TODO: to avoid cast
        this.source = (ParentStreamer<?>) sourceView.getParentStreamer();
        return this;
    }

    public LinkStreamersModel from(ParentStreamer<?> source) {
        this.source = source;
        return this;
    }

    public LinkStreamersModel toView(StreamerView<?> destinationView) {
        this.destinationView = destinationView;
        //TODO: to avoid cast
        this.destination = (ParentStreamer<?>) destinationView.getParentStreamer();
        return this;
    }

    public LinkStreamersModel to(ParentStreamer<?> destination) {
        this.destination = destination;
        return this;
    }

    @Override
    public String getExecutionId() {
        return randomUUID().toString();
    }

    @Override
    public String getActionId() {
        return LINK_STREAMER_ACTION_ID;
    }
}
