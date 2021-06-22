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

package org.cosinus.streamer.ui.action.context;

import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContextProvider;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ActionContextProvider} for {@link StreamerActionContext}
 */
@Component
public class StreamerActionContextProvider implements ActionContextProvider<StreamerActionContext> {

    private final StreamerViewHandler streamerViewHandler;

    public StreamerActionContextProvider(StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public StreamerActionContext provideActionContext() {
        return new StreamerActionContext(streamerViewHandler.getCurrentView(),
                                         streamerViewHandler.getOppositeView());
    }
}
