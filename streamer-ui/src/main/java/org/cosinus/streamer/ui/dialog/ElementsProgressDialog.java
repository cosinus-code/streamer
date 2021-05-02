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

import org.cosinus.streamer.api.Element;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.progress.ElementsProgressModel;
import org.cosinus.swing.util.Formatter;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.window.Frame;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static javax.swing.JLabel.CENTER;

/**
 * Dialog for showing the action progress over multiple elements
 */
public class ElementsProgressDialog<E extends Element> extends ProgressDialog<ElementsProgressModel> {

    private JProgressBar progressBar;

    private JLabel lblElement;

    public ElementsProgressDialog(Frame frame, ActionModel actionModel) {
        super(frame, actionModel);
        init();
    }

    @Override
    public void initComponents() {
        super.initComponents();

        lblElement = new JLabel(actionName + ": " + translator.translate("form_copy_from"));
        actionLabel.setHorizontalAlignment(CENTER);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        JPanel pathsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        pathsPanel.add(actionLabel);
        pathsPanel.add(lblElement);

        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.add(progressBar);

        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.add(pathsPanel, NORTH);
        northPanel.add(progressPanel, SOUTH);

        mainPanel.add(northPanel, NORTH);

        setSize(new Dimension(463, 193));
        centerWindow();
    }

    @Override
    public void setProgress(ElementsProgressModel progressModel) {
        actionLabel.setText(actionName);
        Optional.ofNullable(progressModel.getCurrentStreamer())
            .map(Streamer::getPath)
            .map(path -> Formatter.formatTextForLabel(lblElement, path.toString()))
            .ifPresent(lblElement::setText);
        progressBar.setValue(progressModel.getProgress());
    }
}
