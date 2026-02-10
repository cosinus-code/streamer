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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.StreamerHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.cosinus.streamer.ui.action.execute.copy.CopyActionModel.copy;

@Component
public class CopyHereAction extends DragHereAction {

    public static final String COPY_HERE_ACTION_ID = "copy-here";

    protected CopyHereAction(final StreamerHandler streamerHandler,
                             final ActionExecutors actionExecutors) {
        super(streamerHandler, actionExecutors);
    }

    @Override
    protected ActionModel createActionModel(final List<Streamer<?>> streamerToCopy,
                                            final ParentStreamer<?> source,
                                            final StreamerView<?, ?> destinationView,
                                            final ParentStreamer<?> destination) {
        return copy(streamerToCopy)
            .from(source)
            .to(destination, destinationView);
    }

    @Override
    public String getId() {
        return COPY_HERE_ACTION_ID;
    }
}
