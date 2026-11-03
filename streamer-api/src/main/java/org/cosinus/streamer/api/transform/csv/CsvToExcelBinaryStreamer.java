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

package org.cosinus.streamer.api.transform.csv;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static java.util.stream.IntStream.range;
import static org.apache.poi.ss.SpreadsheetVersion.EXCEL2007;

/**
 * {@link TransformBinaryStreamer} that converts csv content into xlsx content.
 */
public class CsvToExcelBinaryStreamer extends TransformBinaryStreamer<Cvs> {

    private static final String SHEET_NAME = "Sheet1";

    public CsvToExcelBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected Cvs read(InputStream input) throws IOException {
        return new Cvs(input.readAllBytes());
    }

    @Override
    protected void write(Cvs cvs, OutputStream output) throws IOException {
        if (cvs.size() > EXCEL2007.getMaxRows()) {
            throw new IOException("Too many rows for xlsx: %d (maximum %d)"
                .formatted(cvs.size(), EXCEL2007.getMaxRows()));
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            range(0, cvs.size()).forEach(rowIndex -> {
                Row row = sheet.createRow(rowIndex);
                List<String> values = cvs.get(rowIndex);
                range(0, values.size()).forEach(columnIndex -> row
                    .createCell(columnIndex)
                    .setCellValue(values.get(columnIndex)));
            });

            workbook.write(output);
        }
    }
}
