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

package org.cosinus.streamer.ui.view.table.grid;

import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewCreator;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.view.table.grid.GridView.GRID_VIEW_NAME;

@Component
public class GridViewCreator implements StreamerViewCreator {

    @Override
    public StreamerView<?> createStreamerView(PanelLocation location) {
        return new GridView<>(location);
    }

    @Override
    public String getViewName() {
        return GRID_VIEW_NAME;
    }
}
