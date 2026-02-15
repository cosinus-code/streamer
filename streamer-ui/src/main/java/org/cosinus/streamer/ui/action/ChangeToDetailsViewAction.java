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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.ui.action.execute.load.LoadActionExecutor;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.view.table.details.DetailsView.DETAILS_VIEW_NAME;

@Component
public class ChangeToDetailsViewAction extends ChangeViewAction {

    public static final String CHANGE_TO_DETAILS_VIEW_ACTION_ID = "menu-view-details";

    public static final String VIEW_DETAILS_ICON_NAME = "view-details";

    protected ChangeToDetailsViewAction(final StreamerViewHandler streamerViewHandler,
                                        final LoadActionExecutor loadActionExecutor) {
        super(streamerViewHandler, loadActionExecutor);
    }

    @Override
    public ChangeViewActionModel createActionModel() {
        return new ChangeViewActionModel(DETAILS_VIEW_NAME);
    }

    @Override
    public String getIconName() {
        return VIEW_DETAILS_ICON_NAME;
    }

    @Override
    public String getId() {
        return CHANGE_TO_DETAILS_VIEW_ACTION_ID;
    }
}
