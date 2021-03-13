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

package org.cosinus.streamer.ui.dialog;

import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.progress.ProgressListener;
import org.cosinus.streamer.ui.action.progress.ProgressModel;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Generic dialog for showing progress
 */
public abstract class ProgressDialog<P extends ProgressModel>
    extends Dialog<Void> implements ProgressListener<P> {

    protected final ActionModel actionModel;

    protected final JLabel lblAction = new JLabel();

    protected final JButton btnCancel = new JButton();

    protected final JButton btnBackground = new JButton();

    protected JPanel panMain;

    protected boolean background;

    @Autowired
    protected ActionExecutors actionExecutors;

    @Autowired
    protected Translator translator;

    protected final String actionName;

    public ProgressDialog(Frame frame, ActionModel actionModel) {
        super(frame, frame.getTitle() + " " + actionModel.getActionName(), true);
        this.actionModel = actionModel;
        this.actionName = translator.translate(actionModel.getActionName());
    }

    @Override
    public void initComponents() {
        lblAction.setText(translator.translate("action_preparing"));
        btnCancel.setText(translator.translate("form_copy_cancel"));
        btnBackground.setText(translator.translate("form_copy_background"));

        btnCancel.addActionListener(e -> cancel());
        btnBackground.addActionListener(e -> runProgressInBackground());

        JPanel panButtons = new JPanel(new GridLayout(1, 2, 5, 5));
        panButtons.add(btnCancel);
        panButtons.add(btnBackground);

        JPanel panSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panSouth.add(panButtons);

        panMain = new JPanel(new BorderLayout(5, 5));
        panMain.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));
        panMain.add(panSouth, BorderLayout.SOUTH);

        getContentPane().add(panMain);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        btnCancel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        btnBackground.requestFocusInWindow();
                        break;
                    case KeyEvent.VK_ENTER:
                        cancel();
                        break;
                }
            }
        });

        btnBackground.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        btnCancel.requestFocusInWindow();
                        break;
                    case KeyEvent.VK_ENTER:
                        runProgressInBackground();
                        break;
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt) {
                if (!background) cancel();
            }
        });
    }

    @Override
    public void cancel() {
        actionExecutors.getActionExecutor(CopyActionModel.class)
            .ifPresent(executor -> executor.cancel(actionModel.getActionId()));
        super.cancel();
    }

    void runProgressInBackground() {
        background = true;
        dispose();
        //TODO:
        //action.setPriority(Thread.MIN_PRIORITY);
        //dialogHandler.showBackgroundProgress(action);
//        actionExecutors.getActionExecutor(CopyActionModel.class)
//                .ifPresent(executor -> executor.runInBackground(actionModel.getActionId()));
    }

    @Override
    public void startProgress() {
        setVisible(true);
    }

    @Override
    public void finishProgress() {
        dispose();
    }

}
