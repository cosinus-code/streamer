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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

/**
 * {@link TransformBinaryStreamer} that converts excel content (xlsx or xls) into csv content.
 * <p>
 * Only the first sheet is converted, as csv has no notion of sheets.
 * The values are written as they are displayed in excel (formatted numbers and dates, evaluated formulas),
 * comma separated, in UTF-8.
 */
public class ExcelToCsvBinaryStreamer extends TransformBinaryStreamer<Workbook> {

    public ExcelToCsvBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected Workbook read(final InputStream input) throws IOException {
        return WorkbookFactory.create(input);
    }

    @Override
    protected void write(final Workbook workbook,
                         final OutputStream output) throws IOException {
        try (workbook;
             Writer writer = new OutputStreamWriter(output, UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            if (workbook.getNumberOfSheets() == 0) {
                return;
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            rangeClosed(0, sheet.getLastRowNum())
                .mapToObj(sheet::getRow)
                .map(row -> getValues(row, formatter, evaluator))
                .forEach(record -> {
                    try {
                        printer.printRecord(record);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        }
    }

    /**
     * Get the displayed values of a row.
     * Missing rows and cells are kept as empty values, to preserve the layout of the sheet.
     *
     * @param row       the row, or null if missing
     * @param formatter the formatter of the cell values
     * @param evaluator the formula evaluator
     * @return the values of the row
     */
    private List<String> getValues(final Row row,
                                   final DataFormatter formatter,
                                   final FormulaEvaluator evaluator) {
        return row == null || row.getLastCellNum() < 0 ?
            emptyList() :
            range(0, row.getLastCellNum())
                .mapToObj(row::getCell)
                .map(cell -> formatter.formatCellValue(cell, evaluator))
                .toList();
    }
}
