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
import org.cosinus.swing.error.SpringSwingException;
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

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;

/**
 * Panel for controls to find text.
 * <p>
 * When the text is changed, the found results and their highlights are moved to follow the found texts,
 * and, after the text stops changing for {@value #REFRESH_DELAY} milliseconds,
 * the text to find is searched again, to find its new occurrences.
 */
public class FindTextPanel extends FindPanel implements DocumentListener {

    static final int REFRESH_DELAY = 300;

    @Autowired
    private Translator translator;

    @Autowired
    private TextHandler textHandler;

    private final TextEditor textEditor;

    private TextFinder textFinder;

    private Label findResultLabel;

    private Document listenedDocument;

    private final Timer refreshTimer;

    public FindTextPanel(TextEditor textEditor) {
        this.textEditor = textEditor;
        this.refreshTimer = new Timer(REFRESH_DELAY, event -> refreshTextFinder());
        this.refreshTimer.setRepeats(false);
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
                refreshTimer.stop();
                textEditor.removeAllHighlights();
                textFinder = textHandler.createTextFinder(textEditor.getText(), textToFind);
                try (Stream<FindResult> findResults = textFinder.findAll()) {
                    findResults.forEach(textEditor::highlightFoundText);
                }
                listenToDocument();
            }
        } else {
            refreshTimer.stop();
            textFinder = null;
            textEditor.removeAllHighlights();
        }
    }

    @Override
    protected void resetTextFinder() {
        refreshTimer.stop();
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

    /**
     * Listen to the changes of the document of the text editor, which can be replaced (e.g. when a text is loaded).
     */
    private void listenToDocument() {
        Document document = textEditor.getDocument();
        if (document != listenedDocument) {
            ofNullable(listenedDocument).ifPresent(listened -> listened.removeDocumentListener(this));
            listenedDocument = document;
            ofNullable(document).ifPresent(listened -> listened.addDocumentListener(this));
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        moveFindResults(finder -> finder.textInserted(event.getOffset(), insertedText(event)));
    }

    private String insertedText(DocumentEvent event) {
        try {
            return event.getDocument().getText(event.getOffset(), event.getLength());
        } catch (BadLocationException ex) {
            throw new SpringSwingException(ex);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        moveFindResults(finder -> finder.textRemoved(event.getOffset(), event.getLength()));
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        // the attributes of the text don't move the found results
    }

    private void moveFindResults(Function<TextFinder, List<FindResult>> move) {
        if (textFinder != null) {
            move.apply(textFinder).forEach(textEditor::moveFoundTextHighlight);
            refreshTimer.restart();
        }
    }

    /**
     * Search again the text to find in the changed text, keeping the current result and the caret.
     */
    private void refreshTextFinder() {
        if (textFinder != null) {
            List<FindResult> findResults = textFinder.refresh();
            textEditor.removeAllHighlights();
            findResults.forEach(textEditor::highlightFoundText);
            ofNullable(textFinder.getCurrentFind())
                .ifPresent(currentFind -> textEditor.highlightCurrentFoundText(currentFind, false));
            showFindResult();
        }
    }

    private void showFindResult() {
        findResultLabel.setText(ofNullable(textFinder)
            .filter(finder -> finder.count() > 0)
            .map(finder -> finder.currentIndex() + "/" + finder.count())
            .orElseGet(() -> translator.translate("find-no-results")));
    }
}
