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

package org.cosinus.streamer.ui.view.table.details;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewCreator;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.view.table.details.DetailView.DETAIL_VIEW_NAME;

@Component
public class DetailViewCreator<T extends Streamable> implements StreamerViewCreator<T, Streamer<T>> {

    @Override
    public StreamerView<T> createStreamerView(PanelLocation location, Streamer<T> parentStreamer) {
        parentStreamer.initDetails();
        return new DetailView<>(location, parentStreamer);
    }

    @Override
    public String getViewName() {
        return DETAIL_VIEW_NAME;
    }
}
