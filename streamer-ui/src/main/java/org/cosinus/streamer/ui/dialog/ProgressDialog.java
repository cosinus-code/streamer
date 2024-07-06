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

import org.cosinus.streamer.api.worker.WorkerListener;
import org.cosinus.streamer.api.worker.WorkerModel;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.window.Dialog;
import org.cosinus.swing.window.Frame;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.awt.BorderLayout.SOUTH;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * Generic dialog for showing progress
 */
public abstract class ProgressDialog<M extends WorkerModel<M>> extends Dialog<Void> implements WorkerListener<M, M> {

    @Autowired
    protected ActionExecutors actionExecutors;

    @Autowired
    protected Translator translator;

    @Autowired
    protected FormatHandler formatHandler;

    protected final ActionModel actionModel;

    protected JLabel actionLabel;

    protected JButton cancelButton;

    protected JButton runInBackgroundButton;

    protected JPanel mainPanel;

    protected boolean runInBackground;

    protected final String actionName;

    public ProgressDialog(Frame frame, ActionModel actionModel) {
        super(frame, frame.getTitle(), true, false);
        this.actionModel = actionModel;
        this.actionName = translator.translate(actionModel.getActionName());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        actionLabel = new JLabel(translator.translate("action_preparing"));
        cancelButton = new JButton(translator.translate("form_copy_cancel"));
        runInBackgroundButton = new JButton(translator.translate("form_copy_background"));

        cancelButton.addActionListener(e -> cancel());
        runInBackgroundButton.addActionListener(e -> runProgressInBackground());

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(runInBackgroundButton);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        southPanel.add(buttonsPanel);

        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(emptyBorder(5, 25, 5, 25));
        mainPanel.add(southPanel, SOUTH);

        getContentPane().add(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        cancelButton.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        runInBackgroundButton.requestFocusInWindow();
                        break;
                    case KeyEvent.VK_ENTER:
                        cancel();
                        break;
                }
            }
        });

        runInBackgroundButton.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        cancelButton.requestFocusInWindow();
                        break;
                    case KeyEvent.VK_ENTER:
                        runProgressInBackground();
                        break;
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt) {
                if (!runInBackground) cancel();
            }
        });
    }

    @Override
    public void cancel() {
        actionExecutors.getActionExecutor(actionModel)
            .ifPresent(executor -> executor.cancel(actionModel.getActionId()));
        super.cancel();
    }

    void runProgressInBackground() {
        runInBackground = true;
        dispose();
        //TODO:
        //action.setPriority(Thread.MIN_PRIORITY);
        //dialogHandler.showBackgroundProgress(action);
//        actionExecutors.getActionExecutor(CopyActionModel.class)
//                .ifPresent(executor -> executor.runInBackground(actionModel.getActionId()));
    }

    @Override
    public void workerStarted(M workerModel) {
        invokeLater(() -> setVisible(true));
    }

    @Override
    public void workerFinished(M workerModel) {
        dispose();
    }

}
