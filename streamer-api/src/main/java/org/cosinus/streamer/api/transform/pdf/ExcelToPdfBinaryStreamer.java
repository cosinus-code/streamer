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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;

import static java.lang.Math.clamp;
import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

/**
 * {@link TransformBinaryStreamer} that converts excel content (xlsx or xls) into pdf content.
 * <p>
 * Each visible sheet is written as a table, starting on a new landscape A4 page, under the name of the sheet.
 * The values are written as they are displayed in excel (formatted numbers and dates, evaluated formulas),
 * with the numbers aligned to the right.
 * The font is reduced to fit the width of the page, down to a minimum size,
 * below which the columns are narrowed and their texts truncated.
 */
public class ExcelToPdfBinaryStreamer extends TransformBinaryStreamer<Workbook> {

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

    public ExcelToPdfBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected Workbook read(final InputStream input) throws IOException {
        return WorkbookFactory.create(input);
    }

    @Override
    protected void write(final Workbook workbook, final OutputStream output) throws IOException {
        try (workbook;
             PDDocument document = new PDDocument()) {

            PdfTextFont font = PdfTextFont.load(document);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            range(0, workbook.getNumberOfSheets())
                .filter(sheetIndex -> !workbook.isSheetHidden(sheetIndex) && !workbook.isSheetVeryHidden(sheetIndex))
                .mapToObj(workbook::getSheetAt)
                .map(sheet ->
                    new SheetWriter(document, font, sheet.getSheetName(), readRows(sheet, formatter, evaluator, font)))
                .forEach(writer -> {
                    try {
                        writer.write();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

            if (document.getNumberOfPages() == 0) {
                document.addPage(new PDPage(PAGE_SIZE));
            }
            document.save(output);
        }
    }

    private List<List<CellText>> readRows(final Sheet sheet,
                                          final DataFormatter formatter,
                                          final FormulaEvaluator evaluator,
                                          final PdfTextFont font) {
        return rangeClosed(0, sheet.getLastRowNum())
            .mapToObj(sheet::getRow)
            .map(row -> readCells(row, formatter, evaluator, font))
            .toList();
    }

    /**
     * Missing rows and cells are kept as empty texts, to preserve the layout of the sheet.
     */
    private List<CellText> readCells(final Row row,
                                     final DataFormatter formatter,
                                     final FormulaEvaluator evaluator,
                                     final PdfTextFont font) {
        return row == null || row.getLastCellNum() < 0 ?
            emptyList() :
            range(0, row.getLastCellNum())
                .mapToObj(row::getCell)
                .map(cell -> new CellText(
                    font.sanitize(formatter.formatCellValue(cell, evaluator)),
                    isNumeric(cell)))
                .toList();
    }

    private boolean isNumeric(final Cell cell) {
        if (cell == null) {
            return false;
        }
        CellType type = cell.getCellType() == FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        return type == NUMERIC;
    }

    private record CellText(String text, boolean numeric) {
    }

    /**
     * Writes the rows of a sheet as a table, on as many pages as needed.
     */
    private static class SheetWriter {

        private final PDDocument document;

        private final PdfTextFont font;

        private final String sheetName;

        private final List<List<CellText>> rows;

        private float fontSize;

        private float[] columnWidths;

        private PDPageContentStream content;

        private float y;

        private SheetWriter(final PDDocument document,
                            final PdfTextFont font,
                            final String sheetName,
                            final List<List<CellText>> rows) {
            this.document = document;
            this.font = font;
            this.sheetName = font.sanitize(sheetName);
            this.rows = rows;
        }

        private void write() throws IOException {
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
            for (List<CellText> row : rows) {
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
            writeText(sheetName, MARGIN, y - TITLE_FONT_SIZE, TITLE_FONT_SIZE);
            y -= TITLE_FONT_SIZE * 2;
        }

        private void writeRow(final List<CellText> row, final float rowHeight) throws IOException {
            float x = MARGIN;
            float baseline = y - rowHeight + (rowHeight - fontSize) / 2 + fontSize * 0.2f;
            for (int column = 0; column < columnWidths.length; column++) {
                float width = columnWidths[column];
                content.addRect(x, y - rowHeight, width, rowHeight);
                content.stroke();

                if (column < row.size()) {
                    CellText cell = row.get(column);
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
}
