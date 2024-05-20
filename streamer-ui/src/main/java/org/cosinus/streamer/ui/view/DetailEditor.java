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
package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.table.details.DetailStreamerEditor;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyInsets;

public class DetailEditor<T extends Streamable> extends TextField implements FocusListener {

    @Autowired
    private ErrorHandler errorHandler;

    private T itemToBeEdited;

    private final DetailStreamerEditor<T> editor;

    private final int detailIndex;

    private boolean loading;

    public DetailEditor(final DetailStreamerEditor<T> editor, int detailIndex) {
        this.editor = editor;
        this.detailIndex = detailIndex;

        addFocusListener(this);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == VK_ENTER) {
                        itemToBeEdited.updateDetail(detailIndex, getText());
                        editor.save();
                        editor.setVisible(false);
                    } else if (e.getKeyCode() == VK_ESCAPE) {
                        editor.setVisible(false);
                    }
                } catch (Exception ex) {
                    errorHandler.handleError(editor.getView(), ex);
                }
            }
        });

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isLoading()) {
                    editor.setDirty(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isLoading()) {
                    editor.setDirty(true);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isLoading()) {
                    editor.setDirty(true);
                }
            }
        });

        editor.getView().getContainer().add(this);
        validate();

        updateForm();
    }

    public void loadItem(final T itemToBeEdited) {
        this.itemToBeEdited = itemToBeEdited;

        ofNullable(itemToBeEdited.details().get(detailIndex))
            .map(Object::toString)
            .ifPresent(this::setText);
        setEnabled(itemToBeEdited.canUpdateDetail(detailIndex));
    }

    public void updateForm() {
        setMargin(emptyInsets());
    }

    public int getDetailIndex() {
        return detailIndex;
    }

    @Override
    public void focusGained(FocusEvent e) {
        editor.setVisible(true);
        selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {
        setVisible(false);
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }
}
