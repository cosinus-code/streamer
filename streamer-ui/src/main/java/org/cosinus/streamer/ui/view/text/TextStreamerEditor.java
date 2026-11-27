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
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.form.TextEditor;

import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.List;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.cosinus.streamer.ui.view.StreamerView.INACTIVE_COLOR_GRAY_FACTOR;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.color.Colors.grayedColor;
import static org.cosinus.swing.color.SystemColor.TEXT_COMPONENT_SELECTION_BACKGROUND_INACTIVE;

public class TextStreamerEditor extends TextEditor implements LoadWorkerModel<String>, Viewer<String> {

    private TextStreamerView view;

    private final SaveTextWorkerModel saveWorkerModel;

    /**
     * The selection colors of the active editor, saved while the view is not the current one
     */
    private Color activeSelectionColor;

    private Color activeSelectedTextColor;

    public TextStreamerEditor() {
        saveWorkerModel = new SaveTextWorkerModel(this);
    }

    public void initComponent() {
        super.initComponent();
        setBorder(emptyBorder(0, 3, 0, 3));
        int caretBlinkRate = getCaret().getBlinkRate();
        setCaret(new SelectionKeepingCaret());
        getCaret().setBlinkRate(caretBlinkRate);
    }

    @Override
    public void setView(StreamerView<String> streamerView) {
        this.view = (TextStreamerView) streamerView;
    }

    @Override
    public void setActive(boolean active) {
        if (!active && activeSelectionColor == null) {
            activeSelectionColor = getSelectionColor();
            activeSelectedTextColor = getSelectedTextColor();
            setSelectionColor(uiHandler.getColor(
                TEXT_COMPONENT_SELECTION_BACKGROUND_INACTIVE,
                grayedColor(getBackground(), INACTIVE_COLOR_GRAY_FACTOR)));
            setSelectedTextColor(getForeground());
        } else if (active && activeSelectionColor != null) {
            setSelectionColor(activeSelectionColor);
            setSelectedTextColor(activeSelectedTextColor);
            activeSelectionColor = null;
            activeSelectedTextColor = null;
        }
        getCaret().setSelectionVisible(true);
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
    public long getProgressDone() {
        return getText().getBytes().length;
    }

    public Streamer<String> getParentStreamer() {
        return view.getParentStreamer();
    }

    public SaveWorkerModel<String> getSaveWorkerModel() {
        return saveWorkerModel;
    }

    @Override
    public void startLoading(LoadWorkerModel<?> loadWorkerModel) {
        setDirty(false);
        setLoading(true);
    }

    @Override
    public void finishLoading(LoadWorkerModel<?> loadWorkerModel) {
        setCaretPosition(0);
        requestFocus();
        setDirty(false);
        setLoading(false);
    }

    /**
     * Caret keeping the selection visible when the editor loses the focus,
     * so that the selection of an inactive view is still shown, with the inactive selection colors.
     */
    private static class SelectionKeepingCaret extends DefaultCaret {

        @Override
        public void focusLost(FocusEvent event) {
            super.focusLost(event);
            setSelectionVisible(true);
        }
    }
}
