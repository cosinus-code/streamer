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
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingActionWithModel;
import org.springframework.stereotype.Component;

@Component
public class ChangeViewAction implements SwingActionWithModel<ChangeViewActionModel> {

    public static final String CHANGE_VIEW_ACTION_ID = "change-view";

    private final StreamerViewHandler streamerViewHandler;

    public final LoadActionExecutor loadActionExecutor;

    protected ChangeViewAction(final StreamerViewHandler streamerViewHandler,
                               final LoadActionExecutor loadActionExecutor) {
        this.streamerViewHandler = streamerViewHandler;
        this.loadActionExecutor = loadActionExecutor;
    }

    @Override
    public void run(ChangeViewActionModel actionModel) {
        String viewName = actionModel.viewName();
        if (streamerViewHandler.getCurrentView().getName().equals(viewName)) {
            return;
        }

        loadActionExecutor.execute(new LoadActionModel(
            streamerViewHandler.getCurrentLocation(),
            streamerViewHandler.getCurrentView().getParentStreamer(),
            streamerViewHandler.getCurrentView().getCurrentItemIdentifier(),
            viewName));
        streamerViewHandler.setPreferredViewName(streamerViewHandler.getCurrentLocation(), viewName);
    }

    @Override
    public String getId() {
        return CHANGE_VIEW_ACTION_ID;
    }
}
