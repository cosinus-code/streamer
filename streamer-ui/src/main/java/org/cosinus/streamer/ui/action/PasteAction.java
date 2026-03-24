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

import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ComponentSwingAction;
import org.springframework.stereotype.Component;

import javax.swing.*;

import static org.cosinus.swing.action.ActionController.PASTE_ACTION_ID;

@Component
public class PasteAction implements ComponentSwingAction {

    private final StreamerViewHandler streamerViewHandler;

    public PasteAction(final StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public JComponent getComponent() {
        return streamerViewHandler.getCurrentView();
    }

    @Override
    public String getId() {
        return PASTE_ACTION_ID;
    }

    @Override
    public String getIconName() {
        return PASTE_ACTION_ID;
    }
}
