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

package org.cosinus.streamer.ui.dialog;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.copy.CopyProgressModel;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.window.Frame;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static javax.swing.JLabel.CENTER;

/**
 * Dialog for showing the copy action progress
 */
public class CopyProgressDialog<S extends Streamer<S>> extends ProgressDialog<CopyProgressModel<S>> {

    @Autowired
    protected IconHandler iconHandler;

    private JProgressBar itemProgressBar;
    private JProgressBar totalProgressBar;

    private JLabel copyFromLabel;
    private JLabel copyToLabel;

    public CopyProgressDialog(Frame frame, ActionModel actionModel, String workerId) {
        super(frame, actionModel, workerId);
        init();
    }

    @Override
    public void initComponents() {
        super.initComponents();

        copyFromLabel = new JLabel();
        copyToLabel = new JLabel();

        itemProgressBar = new JProgressBar();
        totalProgressBar = new JProgressBar();

        totalProgressBar.setStringPainted(uiHandler.isProgressTextAllowed());
        itemProgressBar.setStringPainted(uiHandler.isProgressTextAllowed());

        JPanel panNorth = new JPanel(new BorderLayout(5, 5));
        JPanel pathsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel progressPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        iconHandler.findIconByResource("transfer-from.png")
            .ifPresent(copyFromLabel::setIcon);
        iconHandler.findIconByResource("transfer-to.png")
            .ifPresent(copyToLabel::setIcon);


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
    }

    @Override
    public void progressUpdated(final CopyProgressModel progress) {
        updateActionStatus(progress);
        updateActionFromTo(progress);
        updateProgressBar(progress);
    }

    protected void updateActionStatus(final CopyProgressModel progress) {
        boolean preparingAction = progress.getProgressDone() <= 0;
        itemProgressBar.setIndeterminate(preparingAction);
        String actionStatus = preparingAction ?
            translator.translate("action_preparing", progress.getTotalItems()) :
            progress.getSpeed() == 0 ?
                translator.translate("form_copying") :
                translator.translate("form_copy_speed",
                    formatHandler.formatMemorySize(progress.getSpeed()),
                    formatHandler.formatTime(progress.getRemainingTime()));
        actionLabel.setText(actionStatus);
    }

    protected void updateActionFromTo(final CopyProgressModel progress) {
        if (progress.getSource() != null && progress.getTarget() != null) {
            copyFromLabel.setText(formatHandler.formatTextForLabel(copyFromLabel,
                progress.getSource().getPath().toString()));
            copyToLabel.setText(formatHandler.formatTextForLabel(copyToLabel,
                progress.getTarget().getPath().toString()));

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
        totalProgressBar.setValue(progress.getProgressPercent());
    }
}