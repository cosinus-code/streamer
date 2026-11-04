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

package org.cosinus.streamer.api.transform.excel;

import org.apache.pdfbox.pdmodel.PDDocument;
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
import org.cosinus.streamer.api.transform.pdf.PdfCell;
import org.cosinus.streamer.api.transform.pdf.PdfTableWriter;
import org.cosinus.streamer.api.transform.pdf.PdfTextFont;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;

/**
 * {@link TransformBinaryStreamer} that converts excel content (xlsx or xls) into pdf content.
 * <p>
 * Each visible sheet is written as a table (see {@link PdfTableWriter}), under the name of the sheet.
 * The values are written as they are displayed in excel (formatted numbers and dates, evaluated formulas),
 * with the numbers aligned to the right.
 */
public class ExcelToPdfBinaryStreamer extends TransformBinaryStreamer<Workbook> {

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
            PdfTableWriter tableWriter = new PdfTableWriter(document, font);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            range(0, workbook.getNumberOfSheets())
                .filter(sheetIndex -> !workbook.isSheetHidden(sheetIndex) && !workbook.isSheetVeryHidden(sheetIndex))
                .mapToObj(workbook::getSheetAt)
                .forEach(sheet -> {
                    try {
                        tableWriter.writeTable(sheet.getSheetName(), readRows(sheet, formatter, evaluator, font));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

            PdfTableWriter.ensurePage(document);
            document.save(output);
        }
    }

    private List<List<PdfCell>> readRows(final Sheet sheet,
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
    private List<PdfCell> readCells(final Row row,
                                     final DataFormatter formatter,
                                     final FormulaEvaluator evaluator,
                                     final PdfTextFont font) {
        return row == null || row.getLastCellNum() < 0 ?
            emptyList() :
            range(0, row.getLastCellNum())
                .mapToObj(row::getCell)
                .map(cell -> new PdfCell(
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
}
