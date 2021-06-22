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
import org.cosinus.streamer.ui.dialog.StreamersProgressDialog;
import org.cosinus.swing.action.execute.ActionModel;
import org.springframework.stereotype.Component;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Handler for progress forms
 */
@Component
public class ProgressFormHandler {

    public <S extends Streamer, T extends Streamer>
    CopyProgressDialog createCopyProgressDialog(ActionModel action) {
        return new CopyProgressDialog(applicationFrame, action);
    }

    public StreamersProgressDialog createStreamersProgressDialog(ActionModel action) {
        return new StreamersProgressDialog(applicationFrame, action);
    }
}
