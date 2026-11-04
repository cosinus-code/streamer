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

import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

/**
 * Writes plain text in a pdf document, line by line, as it is printed.
 * <p>
 * The text is written with a monospaced font (see {@link PdfTextFont#loadMonospace(PDDocument)}),
 * keeping all its spaces, with the tabs expanded to the next multiple of {@value #TAB_SIZE} columns,
 * so that the text aligned with spaces or tabs (e.g. tables, code) stays aligned.
 * A line longer than the page is wrapped, after its last space that fits, or inside a word with no space.
 * The pages are portrait A4, added as needed.
 * When closed, the writer ensures the document has at least a page.
 */
public class PdfPlainTextWriter implements Closeable {

    public static final PDRectangle PAGE_SIZE = A4;

    public static final float MARGIN = 42;

    public static final float FONT_SIZE = 10;

    static final int TAB_SIZE = 8;

    private static final float LINE_HEIGHT = 1.2f;

    private final PDDocument document;

    private final PdfTextFont font;

    private PDPageContentStream content;

    private float y;

    private boolean pageBreak;

    public PdfPlainTextWriter(final PDDocument document, final PdfTextFont font) {
        this.document = document;
        this.font = font;
    }

    /**
     * Write a line of text, on as many lines of the page as needed.
     *
     * @param line the line, without line break
     * @throws IOException if the line cannot be written
     */
    public void writeLine(final String line) throws IOException {
        for (String part : wrap(font.sanitize(expandTabs(line)))) {
            float lineHeight = FONT_SIZE * LINE_HEIGHT;
            if (content == null || pageBreak || y - lineHeight < MARGIN) {
                newPage();
            }
            if (!part.isBlank()) {
                content.beginText();
                content.setFont(font.getFont(), FONT_SIZE);
                content.newLineAtOffset(MARGIN, y - FONT_SIZE);
                content.showText(part);
                content.endText();
            }
            y -= lineHeight;
        }
    }

    /**
     * Continue the next line on a new page.
     * Nothing happens before the first line, so that the document doesn't start with an empty page.
     */
    public void pageBreak() {
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
     * @return the line, with each tab replaced by the spaces up to the next multiple of {@value #TAB_SIZE} columns
     */
    static String expandTabs(final String line) {
        if (line.indexOf('\t') < 0) {
            return line;
        }
        StringBuilder expanded = new StringBuilder();
        int column = 0;
        for (int index = 0; index < line.length(); ) {
            int codePoint = line.codePointAt(index);
            index += Character.charCount(codePoint);
            if (codePoint == '\t') {
                int spaces = TAB_SIZE - column % TAB_SIZE;
                expanded.append(" ".repeat(spaces));
                column += spaces;
            } else {
                expanded.appendCodePoint(codePoint);
                column++;
            }
        }
        return expanded.toString();
    }

    /**
     * Split a line in parts fitting the width of the page,
     * after the last space that fits, or inside a word with no space.
     */
    List<String> wrap(final String line) throws IOException {
        float maxWidth = PAGE_SIZE.getWidth() - 2 * MARGIN;
        List<String> parts = new ArrayList<>();
        String rest = line;
        while (!rest.isEmpty()) {
            // measured only up to the width of the page, as a line can be very long (e.g. minified data)
            int length = fitLength(rest, maxWidth);
            if (length >= rest.length()) {
                break;
            }
            int space = rest.lastIndexOf(' ', length - 1);
            int breakAt = space > 0 && !rest.substring(0, space).isBlank() ? space : length;
            parts.add(rest.substring(0, breakAt));
            rest = breakAt == space ? rest.substring(space + 1) : rest.substring(breakAt);
        }
        parts.add(rest);
        return parts;
    }

    /**
     * @return the length of the longest start of the text fitting the width, but at least one character
     */
    private int fitLength(final String text, final float maxWidth) throws IOException {
        int length = text.offsetByCodePoints(0, 1);
        while (length < text.length()) {
            int next = text.offsetByCodePoints(length, 1);
            if (font.width(text.substring(0, next), FONT_SIZE) > maxWidth) {
                break;
            }
            length = next;
        }
        return length;
    }

    private void newPage() throws IOException {
        if (content != null) {
            content.close();
        }
        PDPage page = new PDPage(PAGE_SIZE);
        document.addPage(page);
        content = new PDPageContentStream(document, page);
        y = PAGE_SIZE.getHeight() - MARGIN;
        pageBreak = false;
    }
}
