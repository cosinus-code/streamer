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

import org.apache.commons.lang3.tuple.Pair;
import org.cosinus.streamer.ui.view.compare.CompareContentHandler;
import org.cosinus.streamer.ui.view.compare.CompareContentHandler.TextContent;
import org.cosinus.streamer.ui.view.compare.DiffRow;
import org.cosinus.streamer.ui.view.compare.DiffRow.Range;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.window.Dialog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.gray;
import static java.awt.Font.BOLD;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.getModifiersExText;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.getKeyText;
import static java.lang.Math.clamp;
import static java.lang.Math.max;
import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.CHANGED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.DELETED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.INSERTED;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.color.Colors.blendColor;
import static org.cosinus.swing.color.Colors.blendColorWithBiTintAlpha;
import static org.cosinus.swing.color.Colors.blendColorWithTintAlpha;
import static org.cosinus.swing.color.Colors.tintAlphaOfColor;
import static org.cosinus.swing.icon.IconSize.X16;
import static org.cosinus.swing.image.icon.IconProvider.ICON_DOWN;
import static org.cosinus.swing.image.icon.IconProvider.ICON_REFRESH;
import static org.cosinus.swing.image.icon.IconProvider.ICON_SWAP;
import static org.cosinus.swing.image.icon.IconProvider.ICON_UP;

/**
 * Dialog showing two texts side-by-side, with the different lines highlighted,
 * and the different words highlighted in the changed lines.
 * <p>
 * The lines are aligned: a line only in one text is shown in front of an empty line in the other one.
 * Both texts scroll vertically together.
 * <p>
 * The toolbar buttons move to the previous or next difference (a block of successive different lines),
 * relative to the line of the caret, also with the Alt+Up and Alt+Down shortcuts.
 * Each of these buttons is disabled if there is no difference in its direction.
 * Another toolbar button, also with the Ctrl+Shift+S shortcut, swaps the left and right sides.
 * The refresh button, also with the Ctrl+R shortcut, reads the texts again in background and compares them again.
 * The dialog opens on the first difference.
 */
public class CompareContentDialog extends Dialog<Void> {

    public static final String COMPARE_CONTENT_TITLE = "compare-content-title";

    public static final String COMPARE_CONTENT_CLOSE = "compare-content-close";

    public static final String COMPARE_CONTENT_IDENTICAL = "compare-content-identical";

    public static final String COMPARE_CONTENT_DIFFERENCES = "compare-content-differences";

    public static final String COMPARE_CONTENT_CURRENT_DIFFERENCE = "compare-content-current-difference";

    public static final String COMPARE_CONTENT_SWAP = "compare-content-swap";

    public static final String COMPARE_CONTENT_REFRESH = "compare-content-refresh";

    public static final String COMPARE_CONTENT_PREVIOUS_DIFFERENCE = "compare-content-previous-difference";

    public static final String COMPARE_CONTENT_NEXT_DIFFERENCE = "compare-content-next-difference";

    private static final KeyStroke PREVIOUS_DIFFERENCE_KEY = KeyStroke.getKeyStroke(VK_UP, ALT_DOWN_MASK);

    private static final KeyStroke NEXT_DIFFERENCE_KEY = KeyStroke.getKeyStroke(VK_DOWN, ALT_DOWN_MASK);

    private static final Color DELETED_TINT = new Color(220, 60, 60);

    private static final Color INSERTED_TINT = new Color(60, 180, 75);

    private static final Color CHANGED_TINT = new Color(70, 130, 220);

    private static final Color MISSING_TINT = new Color(128, 128, 128);

    @Autowired
    private Translator translator;

    @Autowired
    private IconHandler iconHandler;

    @Autowired
    private ErrorHandler errorHandler;

    private TextContent leftContent;

    private List<String> leftLines;

    private TextContent rightContent;

    private List<String> rightLines;

    private List<DiffRow> rows;

    private JLabel leftTitleLabel;

    private JLabel rightTitleLabel;

    private JTextArea leftLineNumbers;

    private JTextArea rightLineNumbers;

    /**
     * The differences, as ranges from the first row to the row after the last one of each block of different rows
     */
    private final List<Range> differences = new ArrayList<>();

    private JTextArea leftText;

    private JTextArea rightText;

    private JScrollPane leftScroll;

    private JButton previousButton;

    private JButton nextButton;

    private JButton refreshButton;

    private JLabel statusLabel;

    private int highlightedDifferenceIndex = -1;

    /**
     * The highlights of the current difference in the left and right texts
     */
    private Pair<Object, Object> currentDifferenceHighlights = Pair.of(null, null);

    private int currentRow;

    /**
     * True while the texts are replaced, to ignore the caret moves
     */
    private boolean updatingTexts;

    /**
     * @param frame        the parent frame
     * @param leftContent  the left text content, read again on refresh
     * @param leftText     the left text, already read
     * @param rightContent the right text content, read again on refresh
     * @param rightText    the right text, already read
     */
    public CompareContentDialog(final Frame frame,
                                final TextContent leftContent,
                                final String leftText,
                                final TextContent rightContent,
                                final String rightText) {
        super(frame, frame.getTitle(), false, true);
        this.leftContent = leftContent;
        this.leftLines = leftText.lines().toList();
        this.rightContent = rightContent;
        this.rightLines = rightText.lines().toList();
        setTitle(translator.translate(COMPARE_CONTENT_TITLE));
    }

    @Override
    public void initComponents() {
        super.initComponents();

        leftText = createTextArea();
        rightText = createTextArea();
        leftLineNumbers = createLineNumbers(leftText);
        rightLineNumbers = createLineNumbers(rightText);
        leftScroll = createScrollPane(leftText, leftLineNumbers);
        JScrollPane rightScroll = createScrollPane(rightText, rightLineNumbers);
        rightScroll.getVerticalScrollBar().setModel(leftScroll.getVerticalScrollBar().getModel());

        JPanel textsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        leftTitleLabel = createTitleLabel();
        rightTitleLabel = createTitleLabel();
        textsPanel.add(createSidePanel(leftTitleLabel, leftScroll));
        textsPanel.add(createSidePanel(rightTitleLabel, rightScroll));

        statusLabel = new JLabel();

        JButton closeButton = new JButton(translator.translate(COMPARE_CONTENT_CLOSE));
        closeButton.addActionListener(event -> close());

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.add(statusLabel, CENTER);
        southPanel.add(closeButton, EAST);

        previousButton = createToolbarButton(ICON_UP, "▲", COMPARE_CONTENT_PREVIOUS_DIFFERENCE,
            PREVIOUS_DIFFERENCE_KEY, this::goToPreviousDifference);
        nextButton = createToolbarButton(ICON_DOWN, "▼", COMPARE_CONTENT_NEXT_DIFFERENCE,
            NEXT_DIFFERENCE_KEY, this::goToNextDifference);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(previousButton);
        toolBar.add(nextButton);
        toolBar.addSeparator();
        KeyStroke swapKey = KeyStroke.getKeyStroke(VK_S, uiHandler.getControlShiftDownKeyMask());
        JButton swapButton = createToolbarButton(ICON_SWAP, "⇄", COMPARE_CONTENT_SWAP, swapKey, this::swapSides);
        toolBar.add(swapButton);
        KeyStroke refreshKey = uiHandler.getControlDownKeyStroke(VK_R);
        refreshButton = createToolbarButton(ICON_REFRESH, "↻", COMPARE_CONTENT_REFRESH, refreshKey, this::refreshComparison);
        toolBar.add(refreshButton);

        leftText.addCaretListener(event -> updateCurrentRow(leftText, event.getDot()));
        rightText.addCaretListener(event -> updateCurrentRow(rightText, event.getDot()));
        showRows(CompareContentHandler.compare(leftLines, rightLines), 0);

        registerShortcut(PREVIOUS_DIFFERENCE_KEY, COMPARE_CONTENT_PREVIOUS_DIFFERENCE, previousButton);
        registerShortcut(NEXT_DIFFERENCE_KEY, COMPARE_CONTENT_NEXT_DIFFERENCE, nextButton);
        registerShortcut(swapKey, COMPARE_CONTENT_SWAP, swapButton);
        registerShortcut(refreshKey, COMPARE_CONTENT_REFRESH, refreshButton);

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(emptyBorder(5));
        mainPanel.add(toolBar, NORTH);
        mainPanel.add(textsPanel, CENTER);
        mainPanel.add(southPanel, SOUTH);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(closeButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        setSize(1000, 700);

        if (!differences.isEmpty()) {
            invokeLater(() -> goToDifference(differences.getFirst()));
        }
    }

    /**
     * Swap the left and right sides, keeping the current row and the scroll position.
     * <p>
     * The rows are swapped instead of compared again, so they stay aligned the same way.
     */
    private void swapSides() {
        TextContent content = leftContent;
        leftContent = rightContent;
        rightContent = content;
        List<String> lines = leftLines;
        leftLines = rightLines;
        rightLines = lines;
        showRowsAtSamePosition(rows.stream()
            .map(DiffRow::swap)
            .toList());
    }

    /**
     * Read both texts again and compare them in background, then show them keeping the current row
     * and the scroll position.
     * <p>
     * The refresh button is disabled while reading.
     */
    private void refreshComparison() {
        refreshButton.setEnabled(false);
        TextContent left = leftContent;
        TextContent right = rightContent;
        new SwingWorker<Comparison, Void>() {
            @Override
            protected Comparison doInBackground() {
                List<String> newLeftLines = left.text().get().lines().toList();
                List<String> newRightLines = right.text().get().lines().toList();
                return new Comparison(newLeftLines, newRightLines, CompareContentHandler.compare(newLeftLines, newRightLines));
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    Comparison comparison = get();
                    if (!isDisplayable() || left != leftContent) {
                        // closed or swapped meanwhile
                        return;
                    }
                    leftLines = comparison.leftLines();
                    rightLines = comparison.rightLines();
                    showRowsAtSamePosition(comparison.rows());
                } catch (ExecutionException ex) {
                    errorHandler.handleError(CompareContentDialog.this, ex.getCause());
                } catch (InterruptedException ex) {
                    currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void showRowsAtSamePosition(List<DiffRow> newRows) {
        Point viewPosition = leftScroll.getViewport().getViewPosition();
        showRows(newRows, currentRow);
        invokeLater(() -> leftScroll.getViewport().setViewPosition(viewPosition));
    }

    /**
     * Show the rows of a comparison in both sides: texts, line numbers, titles, highlights and navigation.
     *
     * @param newRows the rows to show
     * @param row     the row where to put the carets
     */
    private void showRows(List<DiffRow> newRows, int row) {
        rows = newRows;
        initDifferences(rows);

        updatingTexts = true;
        try {
            highlightedDifferenceIndex = -1;
            currentDifferenceHighlights = Pair.of(null, null);
            leftText.getHighlighter().removeAllHighlights();
            rightText.getHighlighter().removeAllHighlights();
            leftText.setText(rowsText(DiffRow::hasLeftLine, diffRow -> leftLines.get(diffRow.leftLine())));
            rightText.setText(rowsText(DiffRow::hasRightLine, diffRow -> rightLines.get(diffRow.rightLine())));
            leftLineNumbers.setText(lineNumbersText(true));
            rightLineNumbers.setText(lineNumbersText(false));
            highlightRows(leftText, rows, true);
            highlightRows(rightText, rows, false);

            currentRow = clamp(row, 0, rows.size() - 1);
            leftText.setCaretPosition(leftText.getLineStartOffset(currentRow));
            rightText.setCaretPosition(rightText.getLineStartOffset(currentRow));
        } catch (BadLocationException ignored) {
            // both texts have the same rows
        } finally {
            updatingTexts = false;
        }

        updateTitle(leftTitleLabel, leftContent.title());
        updateTitle(rightTitleLabel, rightContent.title());
        updateNavigation();
    }

    private void initDifferences(List<DiffRow> rows) {
        differences.clear();
        int index = 0;
        while (index < rows.size()) {
            if (!rows.get(index).isDifferent()) {
                index++;
                continue;
            }
            int start = index;
            while (index < rows.size() && rows.get(index).isDifferent()) {
                index++;
            }
            differences.add(new Range(start, index));
        }
    }

    private JButton createToolbarButton(String iconName,
                                        String fallbackText,
                                        String tooltipKey,
                                        KeyStroke keyStroke,
                                        Runnable action) {
        JButton button = new JButton();
        iconHandler.findIconByName(iconName, X16, false)
            .ifPresentOrElse(button::setIcon, () -> button.setText(fallbackText));
        button.setToolTipText(keyStroke != null ?
            translator.translate(tooltipKey) + " (" + getKeyStrokeText(keyStroke) + ")" :
            translator.translate(tooltipKey));
        button.setFocusable(false);
        button.addActionListener(event -> action.run());
        return button;
    }

    /**
     * Register a shortcut clicking a toolbar button, so it does nothing while the button is disabled.
     * <p>
     * The shortcut is registered in the whole dialog, and also in both texts
     * to take precedence over the text key bindings using the same key stroke.
     */
    private void registerShortcut(KeyStroke keyStroke, String actionKey, JButton button) {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (button.isEnabled()) {
                    button.doClick(0);
                }
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, actionKey);
        getRootPane().getActionMap().put(actionKey, action);
        Stream.of(leftText, rightText).forEach(textArea -> {
            textArea.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, actionKey);
            textArea.getActionMap().put(actionKey, action);
        });
    }

    private String getKeyStrokeText(KeyStroke keyStroke) {
        String modifiers = getModifiersExText(keyStroke.getModifiers());
        String key = getKeyText(keyStroke.getKeyCode());
        return modifiers.isEmpty() ? key : modifiers + "+" + key;
    }

    /**
     * @return the last difference entirely before the current row, if any
     */
    private Range findPreviousDifference() {
        return differences.stream()
            .takeWhile(difference -> difference.end() <= currentRow)
            .reduce((previous, next) -> next)
            .orElse(null);
    }

    /**
     * @return the first difference starting after the current row, if any
     */
    private Range findNextDifference() {
        return differences.stream()
            .filter(difference -> difference.start() > currentRow)
            .findFirst()
            .orElse(null);
    }

    private void goToPreviousDifference() {
        ofNullable(findPreviousDifference()).ifPresent(this::goToDifference);
    }

    private void goToNextDifference() {
        ofNullable(findNextDifference()).ifPresent(this::goToDifference);
    }

    /**
     * Move the caret of both texts at the first row of a difference, and scroll to show it in the upper part.
     */
    private void goToDifference(Range difference) {
        try {
            int row = difference.start();
            leftText.setCaretPosition(leftText.getLineStartOffset(row));
            rightText.setCaretPosition(rightText.getLineStartOffset(row));
            invokeLater(() -> scrollToRow(row));
        } catch (BadLocationException ignored) {
            // the differences and the text lines are built together
        }
    }

    private void scrollToRow(int row) {
        try {
            Rectangle2D rowBounds = leftText.modelToView2D(leftText.getLineStartOffset(row));
            if (rowBounds == null) {
                return;
            }
            JViewport viewport = leftScroll.getViewport();
            int maxY = max(0, leftText.getHeight() - viewport.getExtentSize().height);
            int y = clamp((int) rowBounds.getY() - viewport.getExtentSize().height / 3, 0, maxY);
            viewport.setViewPosition(new Point(viewport.getViewPosition().x, y));
        } catch (BadLocationException ignored) {
            // nothing to scroll to
        }
    }

    private void updateCurrentRow(JTextArea textArea, int caretPosition) {
        if (updatingTexts) {
            return;
        }
        try {
            currentRow = textArea.getLineOfOffset(caretPosition);
        } catch (BadLocationException ignored) {
            currentRow = 0;
        }
        updateNavigation();
    }

    private void updateNavigation() {
        previousButton.setEnabled(findPreviousDifference() != null);
        nextButton.setEnabled(findNextDifference() != null);
        statusLabel.setText(getStatus());
        highlightCurrentDifference();
    }

    /**
     * @return the index of the difference containing the current row, or -1 if the current row is not different
     */
    private int findCurrentDifferenceIndex() {
        return range(0, differences.size())
            .filter(index -> differences.get(index).start() <= currentRow &&
                currentRow < differences.get(index).end())
            .findFirst()
            .orElse(-1);
    }

    /**
     * Outline the rows of the current difference in both texts.
     */
    private void highlightCurrentDifference() {
        int currentDifferenceIndex = findCurrentDifferenceIndex();
        if (currentDifferenceIndex == highlightedDifferenceIndex) {
            return;
        }
        highlightedDifferenceIndex = currentDifferenceIndex;
        currentDifferenceHighlights = Pair.of(
            highlightDifference(leftText, currentDifferenceHighlights.getLeft(), currentDifferenceIndex),
            highlightDifference(rightText, currentDifferenceHighlights.getRight(), currentDifferenceIndex));
    }

    private Object highlightDifference(JTextArea textArea, Object oldHighlight, int differenceIndex) {
        Highlighter highlighter = textArea.getHighlighter();
        ofNullable(oldHighlight).ifPresent(highlighter::removeHighlight);
        Object highlight = null;
        if (differenceIndex >= 0) {
            Range difference = differences.get(differenceIndex);
            try {
                highlight = highlighter.addHighlight(
                    textArea.getLineStartOffset(difference.start()),
                    textArea.getLineEndOffset(difference.end() - 1),
                    new OutlinePainter(blendColor(textArea.getForeground(), CHANGED_TINT, 0.5f)));
            } catch (BadLocationException ignored) {
                // the differences and the text lines are built together
            }
        }
        textArea.repaint();
        return highlight;
    }

    private String rowsText(Predicate<DiffRow> hasLine, Function<DiffRow, String> line) {
        return rows.stream()
            .map(row -> hasLine.test(row) ? line.apply(row) : "")
            .collect(joining("\n"));
    }

    private String lineNumbersText(boolean left) {
        int width = String.valueOf(max(leftLines.size(), rightLines.size())).length();
        return rows.stream()
            .map(row -> left ? row.leftLine() : row.rightLine())
            .map(line -> line == DiffRow.NO_LINE ? "" : String.valueOf(line + 1))
            .map(number -> " ".repeat(width - number.length()) + number + " ")
            .collect(joining("\n"));
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        return textArea;
    }

    private JTextArea createLineNumbers(JTextArea textArea) {
        JTextArea lineNumbers = new JTextArea();
        lineNumbers.setEditable(false);
        lineNumbers.setFocusable(false);
        // the caret must not follow the replaced text, scrolling the line numbers apart from the text
        if (lineNumbers.getCaret() instanceof DefaultCaret caret) {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
        lineNumbers.setFont(textArea.getFont());
        lineNumbers.setForeground(gray);
        lineNumbers.setBackground(uiHandler.getControlColor());
        return lineNumbers;
    }

    private JScrollPane createScrollPane(JTextArea textArea, JTextArea lineNumbers) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.getVerticalScrollBar().setUnitIncrement(textArea.getFontMetrics(textArea.getFont()).getHeight());
        return scrollPane;
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(BOLD));
        titleLabel.setBorder(emptyBorder(2, 2, 2, 2));
        return titleLabel;
    }

    private void updateTitle(JLabel titleLabel, String title) {
        titleLabel.setText(title);
        titleLabel.setToolTipText(title);
    }

    private JPanel createSidePanel(JLabel titleLabel, JScrollPane scrollPane) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(titleLabel, NORTH);
        panel.add(scrollPane, CENTER);
        alignTitleWithText(titleLabel, panel, scrollPane);
        return panel;
    }

    /**
     * Keep the title starting at the same x as the text, after the line numbers,
     * whose width changes with the number of lines.
     */
    private void alignTitleWithText(JLabel titleLabel, JPanel panel, JScrollPane scrollPane) {
        JViewport viewport = scrollPane.getViewport();
        viewport.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent event) {
                updateTitleIndent();
            }

            @Override
            public void componentResized(ComponentEvent event) {
                updateTitleIndent();
            }

            private void updateTitleIndent() {
                int textX = SwingUtilities.convertPoint(viewport, 0, 0, panel).x;
                int indent = textX + ofNullable(viewport.getView())
                    .map(view -> ((JComponent) view).getInsets().left)
                    .orElse(0);
                if (titleLabel.getInsets().left != indent) {
                    titleLabel.setBorder(emptyBorder(2, indent, 2, 2));
                }
            }
        });
    }

    private void highlightRows(JTextArea textArea, List<DiffRow> rows, boolean left) {
        Color background = textArea.getBackground();
        HighlightPainter deletedPainter = new LinePainter(blendColorWithTintAlpha(background, DELETED_TINT));
        HighlightPainter insertedPainter = new LinePainter(blendColorWithTintAlpha(background, INSERTED_TINT));
        HighlightPainter changedPainter = new LinePainter(blendColorWithTintAlpha(background, CHANGED_TINT));
        HighlightPainter missingPainter = new LinePainter(blendColor(background, MISSING_TINT, 0.15f));
        HighlightPainter changedWordsPainter =
            new DefaultHighlightPainter(blendColorWithBiTintAlpha(background, CHANGED_TINT));

        Highlighter highlighter = textArea.getHighlighter();
        range(0, rows.size())
            .forEach(index -> {
                DiffRow row = rows.get(index);
                Highlighter.HighlightPainter painter =
                    row.type() == CHANGED ? changedPainter :
                        row.type() == DELETED ? (left ? deletedPainter : missingPainter) :
                            row.type() == INSERTED ? (left ? missingPainter : insertedPainter) :
                                null;
                if (painter != null) {
                    try {
                        int lineStart = textArea.getLineStartOffset(index);
                        highlighter.addHighlight(lineStart, textArea.getLineEndOffset(index), painter);
                        for (Range range : left ? row.leftRanges() : row.rightRanges()) {
                            highlighter.addHighlight(lineStart + range.start(), lineStart + range.end(),
                                changedWordsPainter);
                        }
                    } catch (BadLocationException ignored) {
                        // the rows and the text lines are built together
                    }
                }
            });
    }

    /**
     * @return the number of the current difference and the number of differences if the caret is in a difference,
     * otherwise the number of differences
     */
    private String getStatus() {
        if (differences.isEmpty()) {
            return translator.translate(COMPARE_CONTENT_IDENTICAL);
        }
        int currentDifferenceIndex = findCurrentDifferenceIndex();
        return currentDifferenceIndex >= 0 ?
            translator.translate(COMPARE_CONTENT_CURRENT_DIFFERENCE, currentDifferenceIndex + 1, differences.size()) :
            translator.translate(COMPARE_CONTENT_DIFFERENCES, differences.size());
    }

    /**
     * The result of a comparison done in background.
     */
    private record Comparison(List<String> leftLines, List<String> rightLines, List<DiffRow> rows) {
    }

    /**
     * Painter drawing an outline around the whole width of the highlighted lines.
     */
    private record OutlinePainter(Color color) implements Highlighter.HighlightPainter {

        private static final int THICKNESS = 2;

        @Override
        public void paint(Graphics graphics, int start, int end, Shape bounds, JTextComponent component) {
            try {
                Rectangle2D first = component.modelToView2D(start);
                Rectangle2D last = component.modelToView2D(max(start, end - 1));
                if (first == null || last == null) {
                    return;
                }
                int y = (int) first.getY();
                int height = (int) (last.getY() + last.getHeight()) - y;
                Graphics2D graphics2D = (Graphics2D) graphics.create();
                try {
                    graphics2D.setColor(color);
                    graphics2D.setStroke(new BasicStroke(THICKNESS));
                    graphics2D.drawRect(THICKNESS / 2, y + THICKNESS / 2,
                        component.getWidth() - THICKNESS, height - THICKNESS);
                } finally {
                    graphics2D.dispose();
                }
            } catch (BadLocationException ignored) {
                // nothing to paint
            }
        }
    }

    /**
     * Painter filling the whole width of the highlighted lines.
     */
    private record LinePainter(Color color) implements Highlighter.HighlightPainter {

        @Override
        public void paint(Graphics graphics, int start, int end, Shape bounds, JTextComponent component) {
            try {
                Rectangle2D first = component.modelToView2D(start);
                Rectangle2D last = component.modelToView2D(max(start, end - 1));
                if (first == null || last == null) {
                    return;
                }
                graphics.setColor(color);
                int y = (int) first.getY();
                graphics.fillRect(0, y, component.getWidth(), (int) (last.getY() + last.getHeight()) - y);
            } catch (BadLocationException ignored) {
                // nothing to paint
            }
        }
    }
}
