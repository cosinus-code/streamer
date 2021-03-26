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

import org.cosinus.streamer.ui.action.progress.CopyProgressModel;
import org.cosinus.streamer.ui.util.Formatter;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.form.Frame;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static javax.swing.JLabel.CENTER;

/**
 * Dialog for showing the copy action progress
 */
public class CopyProgressDialog extends ProgressDialog<CopyProgressModel> {

    private final JProgressBar elementProgressBar = new JProgressBar();
    private final JProgressBar totalProgressBar = new JProgressBar();

    private final JLabel lblFrom = new JLabel();
    private final JLabel lblTo = new JLabel();

    public CopyProgressDialog(Frame frame,
                              ActionModel actionModel) {
        super(frame, actionModel);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        lblFrom.setText(translator.translate("form_copy_from"));
        lblTo.setText(translator.translate("form_copy_to"));

        totalProgressBar.setStringPainted(true);
        elementProgressBar.setStringPainted(true);

        JPanel panNorth = new JPanel(new BorderLayout(5, 5));
        JPanel panPath = new JPanel(new GridLayout(3, 1, 5, 5));
        JPanel panProgress = new JPanel(new GridLayout(2, 1, 5, 5));

        panPath.add(lblAction);
        panPath.add(lblFrom);
        panPath.add(lblTo);

        panProgress.add(elementProgressBar);
        panProgress.add(totalProgressBar);

        panNorth.add(panPath, NORTH);
        panNorth.add(panProgress, SOUTH);

        panMain.add(panNorth, NORTH);

        Dimension progressPreferredSize = new Dimension(10, 26);
        elementProgressBar.setPreferredSize(progressPreferredSize);
        totalProgressBar.setPreferredSize(progressPreferredSize);

        lblAction.setHorizontalAlignment(CENTER);
        lblFrom.setSize(400, 16);
        lblTo.setSize(400, 16);

        setSize(new Dimension(463, 193));
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
        lblAction.setText(actionName + ":" + actionStatus);
    }

    protected void updateActionFromTo(final CopyProgressModel progress) {
        if (progress.getSource() != null && progress.getTarget() != null) {
            lblFrom.setText(Formatter.formatTextForLabel(lblFrom,
                                                         translator.translate("form_copy_from",
                                                                              progress.getSource().getPath())));
            lblTo.setText(Formatter.formatTextForLabel(lblTo,
                                                       translator.translate("form_copy_to",
                                                                            progress.getTarget().getPath())));

            lblFrom.setToolTipText(progress.getSource().getPath().toString());
            lblTo.setToolTipText(progress.getTarget().getPath().toString());
        } else {
            lblFrom.setText("");
            lblTo.setText("");
            lblFrom.setToolTipText("");
            lblTo.setToolTipText("");
        }
    }

    protected void updateProgressBar(final CopyProgressModel progress) {
        elementProgressBar.setValue(progress.getElementProgress());
        totalProgressBar.setValue(progress.getTotalProgress());
    }
}