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

import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.ui.view.table.icon.IconView.ICON_VIEW_NAME;

@Component
public class ChangeToIconViewAction implements ActionInContext {

    private final ChangeViewAction changeViewAction;

    public ChangeToIconViewAction(final ChangeViewAction changeViewAction) {
        this.changeViewAction = changeViewAction;
    }

    @Override
    public void run(ActionContext context) {
        changeViewAction.run(new ChangeViewActionContext(ICON_VIEW_NAME));
    }

    @Override
    public String getIconName() {
        return "view-icon";
    }

    @Override
    public String getId() {
        return "menu-view-icon";
    }
}
