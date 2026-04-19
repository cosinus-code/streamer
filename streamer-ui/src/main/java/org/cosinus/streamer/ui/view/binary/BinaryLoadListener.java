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

package org.cosinus.streamer.ui.view.binary;

import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewLoadWorkerListener;

import java.nio.ByteBuffer;

import static javax.swing.SwingUtilities.invokeLater;

public class BinaryLoadListener extends StreamerViewLoadWorkerListener<ByteBuffer> {

    private final BinaryHexaEditor binaryEditor;

    public BinaryLoadListener(final BinaryStreamerView streamerView) {
        super(streamerView);
        this.binaryEditor = streamerView.getBinaryEditor();
    }

    @Override
    public void workerStarted(LoadWorkerModel<ByteBuffer> loadWorkerModel) {
        super.workerStarted(loadWorkerModel);
    }

    @Override
    public void workerUpdated(LoadWorkerModel<ByteBuffer> loadWorkerModel) {
        super.workerUpdated(loadWorkerModel);
        invokeLater(binaryEditor::repaint);
    }

    @Override
    public void workerFinished(LoadWorkerModel<ByteBuffer> loadWorkerModel) {
        super.workerFinished(loadWorkerModel);
        invokeLater(binaryEditor::repaint);
    }
}
