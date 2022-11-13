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

import org.cosinus.streamer.ui.action.execute.copy.CopyProgressModel;
import org.cosinus.swing.util.Formatter;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.window.Frame;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static javax.swing.JLabel.CENTER;

/**
 * Dialog for showing the copy action progress
 */
public class CopyProgressDialog extends ProgressDialog<CopyProgressModel> {

    private JProgressBar itemProgressBar;
    private JProgressBar totalProgressBar;

    private JLabel copyFromLabel;
    private JLabel copyToLabel;

    public CopyProgressDialog(Frame frame, ActionModel actionModel) {
        super(frame, actionModel);
        init();
    }

    @Override
    public void initComponents() {
        super.initComponents();

        copyFromLabel = new JLabel(translator.translate("form_copy_from"));
        copyToLabel = new JLabel(translator.translate("form_copy_to"));

        itemProgressBar = new JProgressBar();
        totalProgressBar = new JProgressBar();

        totalProgressBar.setStringPainted(true);
        itemProgressBar.setStringPainted(true);

        JPanel panNorth = new JPanel(new BorderLayout(5, 5));
        JPanel pathsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel progressPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        pathsPanel.add(actionLabel);
        pathsPanel.add(copyFromLabel);
        pathsPanel.add(copyToLabel);

        progressPanel.add(itemProgressBar);
        progressPanel.add(totalProgressBar);

        panNorth.add(pathsPanel, NORTH);
        panNorth.add(progressPanel, SOUTH);

        mainPanel.add(panNorth, NORTH);

        actionLabel.setHorizontalAlignment(CENTER);
        copyFromLabel.setSize(400, 16);
        copyToLabel.setSize(400, 16);

        setSize(new Dimension(463, 193));
        centerWindow();
    }

    @Override
    public void setProgress(final CopyProgressModel progress) {
        updateActionStatus(progress);
        updateActionFromTo(progress);
        updateProgressBar(progress);
    }

    protected void updateActionStatus(final CopyProgressModel progress) {
        String actionStatus = progress.getTotalProgress() == 0 ?
            translator.translate("action_preparing") :
            progress.getSpeed() == 0 ?
                "" :
                translator.translate("form_copy_speed",
                                     Formatter.formatMemorySize(progress.getSpeed()),
                                     Formatter.formatTime(progress.getRemainingTime()));
        actionLabel.setText(actionName + ":" + actionStatus);
    }

    protected void updateActionFromTo(final CopyProgressModel progress) {
        if (progress.getSource() != null && progress.getTarget() != null) {
            copyFromLabel.setText(Formatter.formatTextForLabel(copyFromLabel,
                                                               translator.translate("form_copy_from",
                                                                              progress.getSource().getPath())));
            copyToLabel.setText(Formatter.formatTextForLabel(copyToLabel,
                                                             translator.translate("form_copy_to",
                                                                            progress.getTarget().getPath())));

            copyFromLabel.setToolTipText(progress.getSource().getPath().toString());
            copyToLabel.setToolTipText(progress.getTarget().getPath().toString());
        } else {
            copyFromLabel.setText("");
            copyToLabel.setText("");
            copyFromLabel.setToolTipText("");
            copyToLabel.setToolTipText("");
        }
    }

    protected void updateProgressBar(final CopyProgressModel progress) {
        itemProgressBar.setValue(progress.getStreamerProgress());
        totalProgressBar.setValue(progress.getTotalProgress());
    }
}