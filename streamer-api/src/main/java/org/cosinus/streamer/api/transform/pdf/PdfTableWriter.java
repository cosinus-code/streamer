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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static java.lang.Math.clamp;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;

/**
 * Writes tables of texts in a pdf document.
 * <p>
 * A table starts on a new landscape A4 page, under its title, and continues on as many pages as needed.
 * The columns are as wide as their texts, with the numbers aligned to the right.
 * The font is reduced to fit the width of the page, down to a minimum size,
 * below which the columns are narrowed and their texts truncated.
 */
class PdfTableWriter {

    static final PDRectangle PAGE_SIZE = new PDRectangle(A4.getHeight(), A4.getWidth());

    static final float MARGIN = 36;

    static final float FONT_SIZE = 9;

    static final float MIN_FONT_SIZE = 5;

    private static final float TITLE_FONT_SIZE = 12;

    private static final float PADDING = 3;

    private static final float LINE_HEIGHT = 1.5f;

    /**
     * Maximum width of a column, in font sizes, so that a single long text doesn't take the whole page.
     */
    private static final float MAX_COLUMN_EMS = 40;

    private static final String ELLIPSIS = "...";

    /**
     * Tolerance when checking a text fits its column,
     * as the column widths are computed from the same texts, with float rounding.
     */
    private static final float FIT_TOLERANCE = 0.01f;

    private final PDDocument document;

    private final PdfTextFont font;

    private String title;

    private List<List<PdfCell>> rows;

    private float fontSize;

    private float[] columnWidths;

    private PDPageContentStream content;

    private float y;

    PdfTableWriter(final PDDocument document, final PdfTextFont font) {
        this.document = document;
        this.font = font;
    }

    /**
     * Ensure the document has at least a page, as a pdf without page is not valid.
     *
     * @param document the document
     */
    static void ensurePage(final PDDocument document) {
        if (document.getNumberOfPages() == 0) {
            document.addPage(new PDPage(PAGE_SIZE));
        }
    }

    /**
     * Write a table, starting on a new page, under a title.
     *
     * @param title the title, written on top of each page of the table
     * @param rows  the rows of the table, with the cell texts already sanitized for the font
     * @throws IOException if the table cannot be written
     */
    void writeTable(final String title, final List<List<PdfCell>> rows) throws IOException {
        this.title = font.sanitize(title);
        this.rows = rows;
        computeLayout();
        try {
            newPage();
            float rowHeight = fontSize * LINE_HEIGHT;
            rows.forEach(row -> {
                try {
                    if (y - rowHeight < MARGIN) {
                        newPage();
                    }
                    writeRow(row, rowHeight);
                    y -= rowHeight;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } finally {
            if (content != null) {
                content.close();
            }
        }
    }

    /**
     * Compute the font size and the column widths,
     * so that the columns are as wide as their texts and the table fits the width of the page.
     */
    private void computeLayout() throws IOException {
        int columnCount = rows.stream().mapToInt(List::size).max().orElse(0);
        float[] textEms = new float[columnCount];
        for (List<PdfCell> row : rows) {
            for (int column = 0; column < row.size(); column++) {
                textEms[column] = clamp(font.width(row.get(column).text(), 1), textEms[column], MAX_COLUMN_EMS);
            }
        }

        float availableWidth = PAGE_SIZE.getWidth() - 2 * MARGIN;
        float paddings = columnCount * 2 * PADDING;
        float totalEms = 0;
        for (float ems : textEms) {
            totalEms += ems;
        }
        fontSize = totalEms > 0 ?
            clamp((availableWidth - paddings) / totalEms, MIN_FONT_SIZE, FONT_SIZE) :
            FONT_SIZE;

        columnWidths = new float[columnCount];
        float totalWidth = 0;
        for (int column = 0; column < columnCount; column++) {
            columnWidths[column] = fontSize * textEms[column] + 2 * PADDING;
            totalWidth += columnWidths[column];
        }
        if (totalWidth > availableWidth) {
            float ratio = availableWidth / totalWidth;
            for (int column = 0; column < columnCount; column++) {
                columnWidths[column] *= ratio;
            }
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
        writeText(title, MARGIN, y - TITLE_FONT_SIZE, TITLE_FONT_SIZE);
        y -= TITLE_FONT_SIZE * 2;
    }

    private void writeRow(final List<PdfCell> row, final float rowHeight) throws IOException {
        float x = MARGIN;
        float baseline = y - rowHeight + (rowHeight - fontSize) / 2 + fontSize * 0.2f;
        for (int column = 0; column < columnWidths.length; column++) {
            float width = columnWidths[column];
            content.addRect(x, y - rowHeight, width, rowHeight);
            content.stroke();

            if (column < row.size()) {
                PdfCell cell = row.get(column);
                String text = fit(cell.text(), width - 2 * PADDING);
                if (!text.isEmpty()) {
                    float textX = cell.numeric() ?
                        x + width - PADDING - font.width(text, fontSize) :
                        x + PADDING;
                    writeText(text, textX, baseline, fontSize);
                }
            }
            x += width;
        }
    }

    private void writeText(final String text, final float x, final float baseline, final float size)
        throws IOException {
        content.beginText();
        content.setFont(font.getFont(), size);
        content.newLineAtOffset(x, baseline);
        content.showText(text);
        content.endText();
    }

    /**
     * Truncate a text, ending it with "...", so that it fits the given width.
     */
    private String fit(final String text, final float maxWidth) throws IOException {
        if (font.width(text, fontSize) <= maxWidth + FIT_TOLERANCE) {
            return text;
        }
        for (int length = text.length() - 1; length > 0; length--) {
            String truncated = text.substring(0, length) + ELLIPSIS;
            if (font.width(truncated, fontSize) <= maxWidth + FIT_TOLERANCE) {
                return truncated;
            }
        }
        return "";
    }
}
