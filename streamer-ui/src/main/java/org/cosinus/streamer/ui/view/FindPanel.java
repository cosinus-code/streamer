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

import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.FindTextField;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.event.ActionListener;

import static java.awt.BorderLayout.CENTER;

/**
 * Panel for find functionality
 */
public abstract class FindPanel extends Panel {

    @Autowired
    protected Translator translator;

    protected FindTextField findTextField;

    public FindPanel() {
        super(new BorderLayout(0, 0));
    }

    @Override
    public void initComponents() {
        findTextField = new FindTextField(this::performFindAction);

        setLayout(new BorderLayout(0, 0));
        add(findTextField, CENTER);

        registerEscapeAction(this::hidePanel);
        setVisible(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            initTextFinder();
            findTextField.requestFocusInWindow();
        }
    }

    protected ActionListener hidePanel() {
        return event -> {
            setVisible(false);
            resetTextFinder();
        };
    }

    protected void initTextFinder() {
    }

    protected void resetTextFinder() {
    }

    protected abstract void performFindAction();
}
