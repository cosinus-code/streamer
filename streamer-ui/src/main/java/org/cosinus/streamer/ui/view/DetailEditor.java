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
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.*;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyInsets;

public class DetailEditor<T extends Streamable> extends TextField implements FocusListener {

    @Autowired
    private ErrorHandler errorHandler;

    private T itemToBeEdited;

    private final StreamerViewDetailsEditor<T> editor;

    private final int detailIndex;

    public DetailEditor(final StreamerViewDetailsEditor<T> editor, int detailIndex) {
        this.editor = editor;
        this.detailIndex = detailIndex;

        addFocusListener(this);
        addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == VK_ENTER) {
                        itemToBeEdited.updateDetail(detailIndex, getText());
                        editor.saveItem();
                        editor.hideEditor();
                    } else if (e.getKeyCode() == VK_ESCAPE) {
                        editor.hideEditor();
                    }
                } catch (Exception ex) {
                    errorHandler.handleError(editor.getView(), ex);
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
}
