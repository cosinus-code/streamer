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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.error.LoadElementException;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static org.cosinus.swing.border.Borders.insetsEmpty;

public abstract class RenamingStreamerView extends StreamerView<Streamer> {

    protected JTextComponent txtRename;

    private Streamer streamerToBeRenamed;

    @Autowired
    public Translator translator;

    @Autowired
    public DialogHandler dialogHandler;

    @Autowired
    public ErrorHandler errorHandler;

    public RenamingStreamerView(PanelLocation location) {
        super(location);
    }

    @Override
    public void updateForm() {
        super.updateForm();
        if (txtRename != null) {
            txtRename.setMargin(insetsEmpty());
        }
    }

    @Override
    public void initContent() {
        txtRename = getRenameComponent();
        txtRename.setVisible(false);
        txtRename.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                txtRename.setVisible(false);
            }
        });

        txtRename.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_ENTER:
                            renameElement();
                        case KeyEvent.VK_ESCAPE:
                            hideControl();
                    }
                } catch (Exception ex) {
                    errorHandler.handleError(RenamingStreamerView.this, ex);
                }
            }
        });
        super.initContent();
    }

    protected void validateInContainer(Container container) {
        container.add(txtRename);
        txtRename.validate();
    }

    private void hideControl() {
        txtRename.setVisible(false);
        SwingUtilities.invokeLater(RenamingStreamerView.this::requestFocus);
    }

    private void renameElement() throws LoadElementException {
        String newName = getRenameText();
        streamerToBeRenamed.getParent().rename(streamerToBeRenamed.getPath(), newName);
        reload();
        findContent(newName);
    }

    @Override
    public void showRename() {
        if (txtRename == null) return;

        streamerToBeRenamed = getCurrentContent();
        if (streamerToBeRenamed == null) return;
        if (streamerToBeRenamed == getLoadedStreamer()) return;

//        if (!streamerToBeRenamed.streamer().canWriteTo(streamerToBeRenamed)) {
//            dialogHandler.showInfo(translator.translate("rename.info.no.rename"));
//        }

        txtRename.setText(streamerToBeRenamed.getName());
        txtRename.setBounds(getCurrentRectangle());
        txtRename.setVisible(true);
        txtRename.requestFocus();
        txtRename.selectAll();
    }

    protected String getRenameText() {
        return txtRename.getText();
    }

    protected JTextComponent getRenameComponent() {
        return new JTextField();
    }
}
