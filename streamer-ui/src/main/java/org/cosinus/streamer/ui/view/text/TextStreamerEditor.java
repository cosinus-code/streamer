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
package org.cosinus.streamer.ui.view.text;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.swing.form.TextEditor;

import java.util.List;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.cosinus.swing.border.Borders.emptyBorder;

public class TextStreamerEditor extends TextEditor implements LoadWorkerModel<String> {

    private final TextStreamerView view;

    private final SaveTextWorkerModel saveWorkerModel;

    public TextStreamerEditor(final TextStreamerView view) {
        this.view = view;
        saveWorkerModel = new SaveTextWorkerModel(this);
    }

    public void initComponent() {
        super.initComponent();
        setBorder(emptyBorder(0, 3, 0, 3));
    }

    @Override
    public void preventCancelAction() {
        view.preventCancelAction();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        view.updateStreamerViewIdentifiers();
        view.updateStatus();
    }

    @Override
    public void update(List<String> textLines) {
        if (!textLines.isEmpty()) {
            if (!getText().isEmpty()) {
                append(lineSeparator());
            }
            append(join(lineSeparator(), textLines));
        }
        setCaretPosition(0);
    }

    @Override
    public long getLoadedSize() {
        return getText().getBytes().length;
    }

    public Streamer<String> getParentStreamer() {
        return view.getParentStreamer();
    }

    public SaveWorkerModel<String> getSaveWorkerModel() {
        return saveWorkerModel;
    }
}
