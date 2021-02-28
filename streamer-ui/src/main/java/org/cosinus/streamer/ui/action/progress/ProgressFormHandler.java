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

package org.cosinus.streamer.ui.action.progress;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.dialog.CopyProgressDialog;
import org.cosinus.streamer.ui.dialog.ElementsProgressDialog;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.context.SwingApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Handler for progress forms
 */
@Component
public class ProgressFormHandler {

    private final SwingApplicationContext swingContext;

    private final SwingApplicationFrame applicationFrame;

    public ProgressFormHandler(SwingApplicationContext swingContext,
                               SwingApplicationFrame applicationFrame) {
        this.swingContext = swingContext;
        this.applicationFrame = applicationFrame;
    }

    public <S extends Streamer, T extends Streamer>
    CopyProgressDialog createCopyProgressDialog(ActionModel action) {
        return new CopyProgressDialog(swingContext,
                                      applicationFrame,
                                      action);
    }

    public ElementsProgressDialog createElementsProgressDialog(ActionModel action) {
        return new ElementsProgressDialog(swingContext,
                                          applicationFrame,
                                          action);
    }
}
