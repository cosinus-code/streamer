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
package org.cosinus.streamer.ui.view.text;

import org.cosinus.streamer.ui.view.FindPanel;
import org.cosinus.swing.find.FindResult;
import org.cosinus.swing.find.FindText;
import org.cosinus.swing.find.TextFinder;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.TextEditor;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.FindTextField;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.text.TextHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.stream.Stream;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * Panel for controls to find text
 */
public class FindTextPanel extends FindPanel {

    @Autowired
    private Translator translator;

    @Autowired
    private TextHandler textHandler;

    private final TextEditor textEditor;

    private TextFinder textFinder;

    private Label findResultLabel;

    public FindTextPanel(TextEditor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        findResultLabel = new Label();
        findResultLabel.setText(translator.translate("find-no-results"));
        findResultLabel.setPreferredSize(new Dimension(100, 20));

        Button findPreviousButton = new Button("↑", event -> findPrevious());
        Button findNextButton = new Button("↓", event -> findNext());

        Panel findButtonsPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        findButtonsPanel.add(findPreviousButton);
        findButtonsPanel.add(findNextButton);
        findButtonsPanel.add(findResultLabel);

        setBorder(emptyBorder(5, 5, 5, 0));

        add(findButtonsPanel, EAST);
    }

    @Override
    protected void initTextFinder() {
        if (!findTextField.getText().isEmpty()) {
            FindText textToFind = findTextField.getControlValue();
            if (textFinder == null || !textFinder.getTextToFind().equals(textToFind)) {
                textEditor.removeAllHighlights();
                textFinder = textHandler.createTextFinder(textEditor.getText(), textToFind);
                try (Stream<FindResult> findResults = textFinder.findAll()) {
                    findResults.forEach(textEditor::highlightFoundText);
                }
            }
        } else {
            textFinder = null;
            textEditor.removeAllHighlights();
        }
    }

    @Override
    protected void resetTextFinder() {
        textEditor.removeAllHighlights();
        if (textFinder != null) {
            textEditor.selectFoundText(textFinder.getCurrentFind());
        }
        textFinder = null;
        textEditor.preventCancelAction();
        textEditor.requestFocusInWindow();
    }

    @Override
    protected void performFindAction() {
        initTextFinder();
        ofNullable(textFinder)
            .flatMap(finder -> finder.findNext(textEditor.getCaretPosition()))
            .ifPresent(textEditor::highlightCurrentFoundText);
        showFindResult();
    }

    private void findPrevious() {
        ofNullable(textFinder)
            .flatMap(TextFinder::findPrevious)
            .ifPresent(textEditor::highlightCurrentFoundText);
        showFindResult();
    }

    private void findNext() {
        ofNullable(textFinder)
            .flatMap(TextFinder::findNext)
            .ifPresent(textEditor::highlightCurrentFoundText);
        showFindResult();
    }

    private void showFindResult() {
        findResultLabel.setText(ofNullable(textFinder)
            .filter(finder -> finder.count() > 0)
            .map(finder -> finder.currentIndex() + "/" + finder.count())
            .orElseGet(() -> translator.translate("find-no-results")));
    }
}
