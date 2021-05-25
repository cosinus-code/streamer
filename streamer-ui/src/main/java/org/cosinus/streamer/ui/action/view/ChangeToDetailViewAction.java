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

import org.cosinus.streamer.ui.view.StreamerViewCreator;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.table.details.DetailViewCreator;
import org.springframework.stereotype.Component;

@Component
public class ChangeToDetailViewAction extends ChangeViewAction {

    private final DetailViewCreator detailViewCreator;

    public ChangeToDetailViewAction(StreamerViewHandler streamerViewHandler,
                                    DetailViewCreator detailViewCreator) {
        super(streamerViewHandler);
        this.detailViewCreator = detailViewCreator;
    }

    @Override
    protected StreamerViewCreator getStreamerViewCreator() {
        return detailViewCreator;
    }

    @Override
    public String getId() {
        return "menu-view-detail";
    }
}
