/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.api.transform.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

/**
 * Reads the text of a pdf document as word paragraphs.
 * <p>
 * The paragraphs are detected from the line spacing, the font size changes and the indentation,
 * and the lines of a paragraph are joined, so that the text flows again in word.
 * The paragraphs written with a font larger than the body text (the most used font size) are marked as headings.
 * Each pdf page starts on a new page.
 * Only the text is read: the images, the drawings and the layout (e.g. columns, tables) are not kept.
 */
public class PdfWordReader implements Closeable {

    /**
     * Default font size of the body text, when the document has no text.
     */
    static final float BODY_FONT_SIZE = 11;

    /**
     * Ratio to the body font size from which a paragraph is a heading of level 1, respectively 2.
     */
    private static final float HEADING1_RATIO = 1.3f;

    private static final float HEADING2_RATIO = 1.1f;

    /**
     * Maximum length of a heading, so that a paragraph written all in a larger font is not taken as heading.
     */
    private static final int MAX_HEADING_LENGTH = 200;

    /**
     * Vertical distance between two lines, in font sizes, from which they belong to different paragraphs.
     * The lines of a paragraph are usually 1.15 to 1.3 font sizes apart,
     * while the paragraphs have more space between them.
     */
    private static final float PARAGRAPH_GAP = 1.45f;

    /**
     * Font size difference between two lines, in points, from which they belong to different paragraphs.
     */
    private static final float FONT_SIZE_CHANGE = 1;

    private final PDDocument document;

    public PdfWordReader(final byte[] bytes) throws IOException {
        this(Loader.loadPDF(bytes));
    }

    public PdfWordReader(final PDDocument document) {
        this.document = document;
    }

    /**
     * @return the paragraphs of the document
     * @throws IOException if the text cannot be read
     */
    public List<DocxElement> read() throws IOException {
        ParagraphStripper stripper = new ParagraphStripper();
        stripper.writeText(document, Writer.nullWriter());
        float bodyFontSize = stripper.bodyFontSize();
        return stripper.blocks.stream()
            .<DocxElement>map(block -> new DocxParagraph(
                block.text(),
                style(block, bodyFontSize),
                block.fontSize() > 0 ? block.fontSize() : null,
                Alignment.LEFT,
                null,
                0,
                null,
                block.pageBreakBefore()))
            .toList();
    }

    private String style(final TextBlock block, final float bodyFontSize) {
        if (block.text().length() > MAX_HEADING_LENGTH) {
            return "";
        }
        if (block.fontSize() >= bodyFontSize * HEADING1_RATIO) {
            return "heading1";
        }
        if (block.fontSize() >= bodyFontSize * HEADING2_RATIO) {
            return "heading2";
        }
        return "";
    }

    /**
     * A paragraph of text, with the largest font size used in it.
     */
    private record TextBlock(String text, float fontSize, boolean pageBreakBefore) {
    }

    /**
     * Collects the paragraphs found by {@link PDFTextStripper}, instead of writing the text.
     */
    private static class ParagraphStripper extends PDFTextStripper {

        private final List<TextBlock> blocks = new ArrayList<>();

        /**
         * Number of characters written with each font size, rounded to half a point.
         */
        private final Map<Float, Integer> fontSizeCounts = new HashMap<>();

        private final StringBuilder text = new StringBuilder();

        private float fontSize;

        private boolean newLine;

        private boolean pageBreak;

        /**
         * Vertical position and font size of the last line, or null at the start of a page.
         */
        private Float lastLineY;

        private float lastLineFontSize;

        @Override
        protected void startPage(final PDPage page) throws IOException {
            flush();
            pageBreak = !blocks.isEmpty();
            lastLineY = null;
            super.startPage(page);
        }

        @Override
        protected void writeParagraphStart() {
            flush();
        }

        @Override
        protected void writeParagraphEnd() {
            flush();
        }

        @Override
        protected void writeLineSeparator() {
            newLine = !text.isEmpty();
        }

        @Override
        protected void writeWordSeparator() {
            if (!text.isEmpty() && text.charAt(text.length() - 1) != ' ') {
                text.append(' ');
            }
        }

        @Override
        protected void writeString(final String string, final List<TextPosition> positions) {
            if (string.isEmpty() || positions.isEmpty()) {
                return;
            }
            TextPosition first = positions.getFirst();
            float lineFontSize = fontSize(first);
            if (newLine && isParagraphStart(first.getYDirAdj(), lineFontSize)) {
                flush();
            }
            if (lastLineY == null || newLine || first.getYDirAdj() != lastLineY) {
                lastLineY = first.getYDirAdj();
                lastLineFontSize = lineFontSize;
            }
            if (newLine) {
                // a word hyphenated at the end of the line is joined back, keeping the hyphen
                if (!endsWithHyphenatedWord() && text.charAt(text.length() - 1) != ' ') {
                    text.append(' ');
                }
                newLine = false;
            }
            text.append(string);
            for (TextPosition position : positions) {
                if (!position.getUnicode().isBlank()) {
                    float size = fontSize(position);
                    fontSize = max(fontSize, size);
                    fontSizeCounts.merge(size, position.getUnicode().length(), Integer::sum);
                }
            }
        }

        /**
         * A new line starts a paragraph when it is further from the last line than the line spacing
         * (or above it, as in a new column), or when it has another font size.
         */
        private boolean isParagraphStart(final float y, final float fontSize) {
            if (lastLineY == null) {
                return false;
            }
            float gap = y - lastLineY;
            return gap < 0 ||
                gap > lastLineFontSize * PARAGRAPH_GAP ||
                Math.abs(fontSize - lastLineFontSize) >= FONT_SIZE_CHANGE;
        }

        /**
         * @return the font size of a character, in points, rounded to half a point
         */
        private float fontSize(final TextPosition position) {
            return Math.round(position.getFontSizeInPt() * 2) / 2f;
        }

        @Override
        protected void writeString(final String string) {
            // the text is collected in writeString(String, List)
        }

        private boolean endsWithHyphenatedWord() {
            int length = text.length();
            return length > 1 && text.charAt(length - 1) == '-' && Character.isLetter(text.charAt(length - 2));
        }

        private void flush() {
            String paragraph = text.toString().strip();
            if (!paragraph.isEmpty()) {
                blocks.add(new TextBlock(paragraph, fontSize, pageBreak));
                pageBreak = false;
            }
            text.setLength(0);
            fontSize = 0;
            newLine = false;
        }

        /**
         * @return the most used font size, as the size of the body text
         */
        private float bodyFontSize() {
            return fontSizeCounts.entrySet().stream()
                .filter(entry -> entry.getKey() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(BODY_FONT_SIZE);
        }
    }

    @Override
    public void close() throws IOException {
        if (document != null) {
            document.close();
        }
    }
}
