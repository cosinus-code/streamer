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

package org.cosinus.streamer.ui.action.view;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.StreamerAction;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewCreator;
import org.cosinus.streamer.ui.view.StreamerViewHandler;

import static javax.swing.SwingUtilities.invokeLater;

public abstract class ChangeViewAction extends StreamerAction<Streamer<?>> {

    private final StreamerViewHandler streamerViewHandler;

    protected ChangeViewAction(StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run(StreamerActionContext<Streamer<?>> context) {
        StreamerView<Streamer<?>> streamerView = getStreamerViewCreator()
            .createStreamerView(context.getCurrentLocation(), context.getCurrentStreamer());
        streamerViewHandler.getPanel(context.getCurrentLocation())
            .filter(panel -> !panel.getView().getClass().equals(streamerView.getClass()))
            .ifPresent(panel -> {
                panel.setView(streamerView);

                Streamer currentLoadedStreamer = context.getCurrentView().getLoadedStreamer();
                invokeLater(() -> streamerView.loadStreamer(currentLoadedStreamer));
//TODO
//            Streamer currentStreamer = context.getCurrentView().getCurrentContent();
//            streamerView.findContent(currentStreamer.getName());
            });
    }

    protected abstract StreamerViewCreator getStreamerViewCreator();
}
