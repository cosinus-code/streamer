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
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerModel;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.TextArea;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import static java.awt.event.KeyEvent.*;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.cosinus.streamer.ui.action.GoToParentStreamerAction.GO_TO_PARENT_ACTION;

public class TextEditor extends TextArea implements LoadWorkerModel<String> {

    @Autowired
    private ErrorHandler errorHandler;

    @Autowired
    private ActionController actionController;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    private final TextStreamerView view;

    private final SaveTextModel saveWorkerModel;

    private boolean dirty;

    private boolean loading;

    public TextEditor(final TextStreamerView view) {
        this.view = view;
        saveWorkerModel = new SaveTextModel(this);
    }

    public void initComponent() {
        setTabSize(2);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (!isEditorKey(keyEvent)) {
                    actionController.runActionByKeyStroke(keyEvent);
                }
                if (keyEvent.getKeyCode() == VK_ESCAPE) {
                    actionController.runAction(GO_TO_PARENT_ACTION);
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

    public boolean isEditorKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == VK_ENTER
            || keyEvent.getKeyCode() == VK_TAB
            || keyEvent.getKeyCode() == VK_DELETE
            || keyEvent.getKeyCode() == VK_BACK_SPACE;
    }

    protected String getLineAtIndex(int index) {
        try {
            int start = getLineStartOffset(index);
            int end = getLineEndOffset(index);
            return getText(start, end - start);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Document createDefaultModel() {
        return new PlainDocument() {
            public void insertString(int offs, String text, AttributeSet attributes) throws BadLocationException {
                if (!isLoading()) {
                    setDirty(true);
                }
                super.insertString(offs, text, attributes);
            }

            protected void insertUpdate(DefaultDocumentEvent documentEvent, AttributeSet attributes) {
                if (!isLoading()) {
                    setDirty(true);
                }
                super.insertUpdate(documentEvent, attributes);
            }

            protected void removeUpdate(DefaultDocumentEvent documentEvent) {
                if (!isLoading()) {
                    setDirty(true);
                }
                super.removeUpdate(documentEvent);
            }
        };
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        view.updateAddressBarAndStreamerPanel();
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
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

    public void reset() {
        setText("");
    }
}
