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
package org.cosinus.streamer.ui.view.text;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.TextEditor;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;
import static org.cosinus.swing.border.Borders.emptyBorder;

public class TextStreamerEditor extends TextEditor implements LoadWorkerModel<String> {

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private ActionController actionController;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    private final TextStreamerView view;

    private final SaveTextWorkerModel saveWorkerModel;

    private boolean cancelActionPrevented;

    public TextStreamerEditor(final TextStreamerView view) {
        this.view = view;
        saveWorkerModel = new SaveTextWorkerModel(this);
    }

    public void initComponent() {
        super.initComponent();
        setBorder(emptyBorder(0, 3, 0, 3));
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (!isEditorKey(keyEvent)) {
                    actionController.runActionByKeyStroke(keyEvent);
                }
                if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    if (!cancelActionPrevented) {
                        actionController.runAction(GO_TO_PARENT_ACTION);
                    }
                    cancelActionPrevented = false;
                }
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                try {
                    streamerViewHandler.setCurrentLocation(view.getCurrentLocation());
                } catch (Exception ex) {
                    errorHandler.handleError(view, ex);
                }
            }
        });
    }

    @Override
    public void preventCancelAction() {
        cancelActionPrevented = true;
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        view.updateAddressBarAndStreamerPanel();
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

    @Override
    public Streamer<String> getParentStreamer() {
        return view.getParentStreamer();
    }

    @Override
    public String getContentIdentifier() {
        return null;
    }

    @Override
    public void setContentIdentifier(String contentIdentifier) {

    }

    public SaveWorkerModel<String> getSaveWorkerModel() {
        return saveWorkerModel;
    }
}
