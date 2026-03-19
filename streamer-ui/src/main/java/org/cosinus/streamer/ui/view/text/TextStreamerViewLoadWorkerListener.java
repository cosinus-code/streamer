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

package org.cosinus.streamer.ui.view.text;

import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewLoadWorkerListener;

public class TextStreamerViewLoadWorkerListener extends StreamerViewLoadWorkerListener<String, String> {

    private final TextStreamerEditor textEditor;

    public TextStreamerViewLoadWorkerListener(final TextStreamerView streamerView) {
        super(streamerView);
        textEditor = streamerView.getTextEditor();
    }

    @Override
    public void workerStarted(LoadWorkerModel<String> workerModel) {
        super.workerStarted(workerModel);
        textEditor.setDirty(false);
        textEditor.setLoading(true);
    }

    @Override
    public void workerUpdated(LoadWorkerModel<String> workerModel) {
        super.workerUpdated(workerModel);
    }

    @Override
    public void workerFinished(LoadWorkerModel<String> workerModel) {
        super.workerFinished(workerModel);
        textEditor.setCaretPosition(0);
        textEditor.requestFocus();
        textEditor.setDirty(false);
        textEditor.setLoading(false);
    }
}
