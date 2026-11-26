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

package org.cosinus.streamer.ui.view.compare;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.TextStreamer;
import org.cosinus.streamer.ui.dialog.CompareContentDialog;
import org.cosinus.streamer.ui.view.compare.DiffRow.Range;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.streamer.ui.view.text.TextStreamerView;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.difflib.text.DiffRowGenerator.SPLITTER_BY_WORD;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.CHANGED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.DELETED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.EQUAL;
import static org.cosinus.streamer.ui.view.compare.DiffRow.DiffType.INSERTED;
import static org.cosinus.streamer.ui.view.compare.DiffRow.NO_LINE;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

/**
 * Handler finding the text contents to compare and showing them side-by-side in a {@link CompareContentDialog}.
 * <p>
 * For a text view, its whole text is compared, as currently edited.
 * For the other views, the current streamer (or the shown one, if there is no current streamer)
 * is compared only if it is a text streamer: its text is read in background.
 */
@Component
public class CompareContentHandler {

    public static final String COMPARE_CONTENT_CLIPBOARD_TITLE = "compare-content-clipboard-title";

    private static final String TEXT_LINE_SEPARATOR = "\n";

    private final StreamerViewHandler streamerViewHandler;

    private final DialogHandler dialogHandler;

    private final ErrorHandler errorHandler;

    private final Translator translator;

    public CompareContentHandler(final StreamerViewHandler streamerViewHandler,
                                 final DialogHandler dialogHandler,
                                 final ErrorHandler errorHandler,
                                 final Translator translator) {
        this.streamerViewHandler = streamerViewHandler;
        this.dialogHandler = dialogHandler;
        this.errorHandler = errorHandler;
        this.translator = translator;
    }

    /**
     * Find the text content of the view in a panel location.
     *
     * @param location the panel location
     * @return the text content, or empty if the view has no text streamer
     */
    public Optional<TextContent> findTextContent(final PanelLocation location) {
        return streamerViewHandler.getView(location)
            .flatMap(view -> view instanceof TextStreamerView textView ?
                textViewContent(textView) :
                textStreamerContent(view));
    }

    /**
     * Find the text content of the system clipboard.
     *
     * @return the clipboard text content, or empty if the clipboard has no text
     */
    public Optional<TextContent> findClipboardContent() {
        try {
            Clipboard clipboard = getDefaultToolkit().getSystemClipboard();
            if (!clipboard.isDataFlavorAvailable(stringFlavor)) {
                return empty();
            }
            return readClipboardText(clipboard)
                .map(clipboardText -> new TextContent(
                    translator.translate(COMPARE_CONTENT_CLIPBOARD_TITLE),
                    () -> readClipboardText(clipboard).orElse("")));
        } catch (IllegalStateException ex) {
            return empty();
        }
    }

    private Optional<String> readClipboardText(Clipboard clipboard) {
        try {
            return ofNullable((String) clipboard.getData(stringFlavor));
        } catch (IllegalStateException | UnsupportedFlavorException | IOException ex) {
            return empty();
        }
    }

    /**
     * Read the texts in background, then compare them in a dialog.
     *
     * @param left  the left text content
     * @param right the right text content
     */
    public void compare(final TextContent left, final TextContent right) {
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() {
                return new String[]{left.text().get(), right.text().get()};
            }

            @Override
            protected void done() {
                try {
                    String[] texts = get();
                    dialogHandler.showDialog(() -> new CompareContentDialog(applicationFrame,
                        left, texts[0],
                        right, texts[1]));
                } catch (ExecutionException ex) {
                    errorHandler.handleError(applicationFrame, ex.getCause());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /**
     * Compare two lists of lines, aligned for a side-by-side view.
     * <p>
     * In each block of different lines, the first left lines are paired with the first right lines as changed lines,
     * and the remaining ones are deleted or inserted lines.
     * In the changed lines, the different words are also computed.
     *
     * @param left  the left lines
     * @param right the right lines
     * @return the aligned rows
     */
    public static List<DiffRow> compare(final List<String> left, final List<String> right) {
        List<DiffRow> rows = new ArrayList<>(Math.max(left.size(), right.size()));
        int leftLine = 0;
        int rightLine = 0;
        for (AbstractDelta<String> delta : DiffUtils.diff(left, right).getDeltas()) {
            Chunk<String> source = delta.getSource();
            Chunk<String> target = delta.getTarget();
            while (leftLine < source.getPosition()) {
                rows.add(new DiffRow(EQUAL, leftLine++, rightLine++));
            }

            int changed = Math.min(source.size(), target.size());
            for (int row = 0; row < changed; row++) {
                rows.add(changedRow(leftLine++, left.get(source.getPosition() + row),
                    rightLine++, right.get(target.getPosition() + row)));
            }
            for (int row = changed; row < source.size(); row++) {
                rows.add(new DiffRow(DELETED, leftLine++, NO_LINE));
            }
            for (int row = changed; row < target.size(); row++) {
                rows.add(new DiffRow(INSERTED, NO_LINE, rightLine++));
            }
        }
        while (leftLine < left.size()) {
            rows.add(new DiffRow(EQUAL, leftLine++, rightLine++));
        }
        return rows;
    }

    private static DiffRow changedRow(int leftLine, String leftText, int rightLine, String rightText) {
        List<String> leftWords = SPLITTER_BY_WORD.apply(leftText);
        List<String> rightWords = SPLITTER_BY_WORD.apply(rightText);
        List<Range> leftRanges = new ArrayList<>();
        List<Range> rightRanges = new ArrayList<>();
        for (AbstractDelta<String> delta : DiffUtils.diff(leftWords, rightWords).getDeltas()) {
            addRange(leftRanges, leftWords, delta.getSource());
            addRange(rightRanges, rightWords, delta.getTarget());
        }
        return new DiffRow(CHANGED, leftLine, rightLine, leftRanges, rightRanges);
    }

    private static void addRange(List<Range> ranges, List<String> words, Chunk<String> chunk) {
        if (chunk.size() == 0) {
            return;
        }
        int start = length(words, 0, chunk.getPosition());
        int end = start + length(words, chunk.getPosition(), chunk.getPosition() + chunk.size());
        ranges.add(new Range(start, end));
    }

    private static int length(List<String> words, int from, int to) {
        int length = 0;
        for (int index = from; index < to; index++) {
            length += words.get(index).length();
        }
        return length;
    }

    /**
     * Inform the user that there is nothing to compare.
     *
     * @param messageKey the key of the message to show
     */
    public void showNothingToCompare(final String messageKey) {
        dialogHandler.showInfo(applicationFrame, translator.translate(messageKey));
    }

    private Optional<TextContent> textViewContent(TextStreamerView textView) {
        return ofNullable(textView.getTextEditor())
            .map(textEditor -> new TextContent(
                getTitle(textView.getParentStreamer()),
                textEditor::getText));
    }

    private Optional<TextContent> textStreamerContent(StreamerView<?> view) {
        return ofNullable(view.getCurrentStreamerOrParent())
            .filter(Streamer::isTextCompatible)
            .filter(streamer -> streamer.binaryStreamer() != null)
            .map(streamer -> new TextContent(
                getTitle(streamer),
                () -> readText(streamer)));
    }

    private String readText(Streamer<?> streamer) {
        try (Stream<String> lines = new TextStreamer(streamer.binaryStreamer()).stream()) {
            return lines.collect(joining(TEXT_LINE_SEPARATOR));
        }
    }

    private String getTitle(Streamer<?> streamer) {
        return ofNullable(streamer)
            .map(Streamer::getPath)
            .map(Object::toString)
            .orElse("");
    }

    /**
     * A text content to compare.
     *
     * @param title the title shown for the content
     * @param text  the supplier of the text, called in background, and again on each refresh of the comparison
     */
    public record TextContent(String title, Supplier<String> text) {
    }
}
