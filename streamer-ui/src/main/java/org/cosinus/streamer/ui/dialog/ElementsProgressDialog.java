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
import org.cosinus.streamer.ui.util.Formatter;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.form.Frame;

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

    private final JProgressBar progressBar = new JProgressBar();

    private final JLabel lblElement = new JLabel();

    public ElementsProgressDialog(SwingApplicationContext swingContext,
                                  Frame frame,
                                  ActionModel actionModel) {
        super(swingContext, frame, actionModel);
        initComponents();
    }

    @Override
    public void initComponents() {
        super.initComponents();

        lblAction.setText(actionName + ": " + translator.translate("form_copy_from"));

        progressBar.setStringPainted(true);

        JPanel panNorth = new JPanel(new BorderLayout(5, 5));
        JPanel panPath = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel panProgress = new JPanel(new BorderLayout(5, 5));

        panPath.add(lblAction);
        panPath.add(lblElement);

        panProgress.add(progressBar);

        panNorth.add(panPath, NORTH);
        panNorth.add(panProgress, SOUTH);

        panMain.add(panNorth, NORTH);

        Dimension dim = new Dimension(10, 26);
        progressBar.setPreferredSize(dim);

        lblAction.setHorizontalAlignment(CENTER);
        lblElement.setSize(400, 16);

        setSize(new Dimension(463, 193));
    }

    @Override
    public void setProgress(ElementsProgressModel progressModel) {
        lblAction.setText(actionName);
        Optional.ofNullable(progressModel.getCurrentStreamer())
                .map(Streamer::getPath)
                .map(path -> Formatter.formatTextForLabel(lblElement, path.toString()))
                .ifPresent(lblElement::setText);
        progressBar.setValue(progressModel.getProgress());
    }
}
