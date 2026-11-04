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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

/**
 * Writes flowing text in a pdf document: paragraphs wrapped to the width of the page, and simple tables.
 * <p>
 * The pages are portrait A4, added as needed.
 * The tables have equal column widths, with the cell texts wrapped to the width of their columns.
 * When closed, the writer ensures the document has at least a page.
 */
class PdfTextWriter implements Closeable {

    static final PDRectangle PAGE_SIZE = A4;

    static final float MARGIN = 56;

    static final float FONT_SIZE = 11;

    static final float TABLE_FONT_SIZE = 10;

    /**
     * Indentation of a list level, in points.
     */
    static final float INDENT = 18;

    private static final float LINE_HEIGHT = 1.3f;

    /**
     * Space after a paragraph or a table, in font sizes.
     */
    private static final float PARAGRAPH_SPACING = 0.5f;

    private static final float CELL_PADDING = 3;

    enum Alignment {
        LEFT, CENTER, RIGHT
    }

    private final PDDocument document;

    private final PdfTextFont font;

    private PDPageContentStream content;

    private float y;

    private boolean pageBreak;

    PdfTextWriter(final PDDocument document, final PdfTextFont font) {
        this.document = document;
        this.font = font;
    }

    /**
     * Write a paragraph.
     *
     * @param text      the text, with line breaks as '\n'
     * @param fontSize  the font size
     * @param indent    the indentation from the left margin, in points
     * @param alignment the alignment of the lines
     * @param prefix    the text written before the first line (e.g. a bullet), or null;
     *                  the lines of the paragraph are then indented after the prefix
     * @throws IOException if the paragraph cannot be written
     */
    void writeParagraph(final String text,
                        final float fontSize,
                        final float indent,
                        final Alignment alignment,
                        final String prefix) throws IOException {
        String sanitizedPrefix = prefix == null ? null : font.sanitize(prefix);
        float prefixX = MARGIN + indent;
        float textX = sanitizedPrefix == null ?
            prefixX :
            prefixX + max(INDENT, font.width(sanitizedPrefix, fontSize) + fontSize / 2);
        float width = max(PAGE_SIZE.getWidth() - MARGIN - textX, fontSize);
        float lineHeight = fontSize * LINE_HEIGHT;

        List<String> lines = wrap(text, fontSize, width);
        for (int index = 0; index < lines.size(); index++) {
            ensureSpace(lineHeight);
            float baseline = baseline(y, lineHeight, fontSize);
            if (index == 0 && sanitizedPrefix != null) {
                writeText(sanitizedPrefix, prefixX, baseline, fontSize);
            }
            String line = lines.get(index);
            if (!line.isEmpty()) {
                float lineWidth = font.width(line, fontSize);
                float x = switch (alignment) {
                    case LEFT -> textX;
                    case CENTER -> textX + max(0, (width - lineWidth) / 2);
                    case RIGHT -> textX + max(0, width - lineWidth);
                };
                writeText(line, x, baseline, fontSize);
            }
            y -= lineHeight;
        }
        y -= fontSize * PARAGRAPH_SPACING;
    }

    /**
     * Write a table, continuing on the next pages if needed.
     * A row is never split between pages,
     * and the lines of a cell which don't fit an entire page are dropped.
     *
     * @param rows the rows of the table, as cell texts with line breaks as '\n'
     * @throws IOException if the table cannot be written
     */
    void writeTable(final List<List<String>> rows) throws IOException {
        int columnCount = rows.stream().mapToInt(List::size).max().orElse(0);
        if (columnCount == 0) {
            return;
        }
        float fontSize = TABLE_FONT_SIZE;
        float lineHeight = fontSize * LINE_HEIGHT;
        float columnWidth = (PAGE_SIZE.getWidth() - 2 * MARGIN) / columnCount;
        float textWidth = max(columnWidth - 2 * CELL_PADDING, 1);
        int maxLines = max(1, (int) ((PAGE_SIZE.getHeight() - 2 * MARGIN - 2 * CELL_PADDING) / lineHeight));

        for (List<String> row : rows) {
            List<List<String>> cellLines = new ArrayList<>(row.size());
            for (String cell : row) {
                cellLines.add(wrap(cell, fontSize, textWidth));
            }
            int lineCount = min(maxLines, max(1, cellLines.stream().mapToInt(List::size).max().orElse(1)));
            float rowHeight = lineCount * lineHeight + 2 * CELL_PADDING;
            ensureSpace(rowHeight);

            for (int column = 0; column < columnCount; column++) {
                float x = MARGIN + column * columnWidth;
                content.addRect(x, y - rowHeight, columnWidth, rowHeight);
                content.stroke();
                if (column < cellLines.size()) {
                    List<String> lines = cellLines.get(column);
                    for (int line = 0; line < min(lineCount, lines.size()); line++) {
                        if (!lines.get(line).isEmpty()) {
                            float lineTop = y - CELL_PADDING - line * lineHeight;
                            writeText(lines.get(line), x + CELL_PADDING, baseline(lineTop, lineHeight, fontSize), fontSize);
                        }
                    }
                }
            }
            y -= rowHeight;
        }
        y -= fontSize * PARAGRAPH_SPACING;
    }

    /**
     * Continue the next text on a new page.
     * Nothing happens before the first text, so that the document doesn't start with an empty page.
     */
    void pageBreak() {
        pageBreak = content != null;
    }

    @Override
    public void close() throws IOException {
        if (content != null) {
            content.close();
            content = null;
        }
        if (document.getNumberOfPages() == 0) {
            document.addPage(new PDPage(PAGE_SIZE));
        }
    }

    /**
     * Split a text in lines fitting the given width:
     * on the line breaks, then between the words,
     * and inside the words which are wider than a line.
     */
    List<String> wrap(final String text, final float fontSize, final float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraphLine : (text == null ? "" : text).split("\\R", -1)) {
            String current = "";
            for (String word : font.sanitize(paragraphLine).split(" +")) {
                if (word.isEmpty()) {
                    continue;
                }
                String candidate = current.isEmpty() ? word : current + " " + word;
                if (font.width(candidate, fontSize) <= maxWidth) {
                    current = candidate;
                    continue;
                }
                if (!current.isEmpty()) {
                    lines.add(current);
                }
                while (font.width(word, fontSize) > maxWidth) {
                    int length = fitLength(word, fontSize, maxWidth);
                    lines.add(word.substring(0, length));
                    word = word.substring(length);
                }
                current = word;
            }
            lines.add(current);
        }
        return lines;
    }

    /**
     * @return the length of the longest start of the word fitting the width, but at least one character
     */
    private int fitLength(final String word, final float fontSize, final float maxWidth) throws IOException {
        int length = word.offsetByCodePoints(0, 1);
        while (length < word.length()) {
            int next = word.offsetByCodePoints(length, 1);
            if (font.width(word.substring(0, next), fontSize) > maxWidth) {
                break;
            }
            length = next;
        }
        return length;
    }

    private void ensureSpace(final float height) throws IOException {
        if (content == null || pageBreak || y - height < MARGIN) {
            newPage();
        }
    }

    private void newPage() throws IOException {
        if (content != null) {
            content.close();
        }
        PDPage page = new PDPage(PAGE_SIZE);
        document.addPage(page);
        content = new PDPageContentStream(document, page);
        content.setLineWidth(0.25f);
        content.setStrokingColor(0.75f, 0.75f, 0.75f);
        y = PAGE_SIZE.getHeight() - MARGIN;
        pageBreak = false;
    }

    private float baseline(final float top, final float lineHeight, final float fontSize) {
        return top - lineHeight + (lineHeight - fontSize) / 2 + fontSize * 0.2f;
    }

    private void writeText(final String text, final float x, final float baseline, final float size)
        throws IOException {
        content.beginText();
        content.setFont(font.getFont(), size);
        content.newLineAtOffset(x, baseline);
        content.showText(text);
        content.endText();
    }
}
