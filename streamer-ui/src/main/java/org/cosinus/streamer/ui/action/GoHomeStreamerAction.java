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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.context.StreamerActionContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_HOME;

/**
 * Go up element action
 */
@Component
public class GoHomeStreamerAction extends StreamerAction<Streamer<?>> {

    public static final String GO_HOME_ELEMENT_ACTION = "go-home";

    @Override
    public void run(StreamerActionContext<Streamer<?>> context) {
        context.getCurrentView().goHome();
    }

    @Override
    public String getId() {
        return GO_HOME_ELEMENT_ACTION;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(KeyStroke.getKeyStroke(VK_HOME, 0));
    }
}
