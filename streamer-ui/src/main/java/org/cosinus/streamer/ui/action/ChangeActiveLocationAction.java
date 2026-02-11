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

import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_TAB;

/**
 * Change active location action
 */
@Component
public class ChangeActiveLocationAction implements SwingAction {

    public static final String CHANGE_ACTIVE_LOCATION_ACTION = "change-active-location";

    private final StreamerViewHandler streamerViewHandler;

    public ChangeActiveLocationAction(final StreamerViewHandler streamerViewHandler) {
        this.streamerViewHandler = streamerViewHandler;
    }

    @Override
    public void run() {
        streamerViewHandler.getOppositeView().requestFocus();
    }

    @Override
    public String getId() {
        return CHANGE_ACTIVE_LOCATION_ACTION;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_TAB, 0));
    }
}
