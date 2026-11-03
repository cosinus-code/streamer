/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.streamer.api.transform.csv;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ExcelToCsvBinaryStreamerTransformerTest {

    private static final String EXPECTED_CSV =
        "name,qty,note\r\n" +
            "\"Ștefan, Ion\",3,\"say \"\"hi\"\"\"\r\n" +
            "\r\n" +
            "\"\",6,2026-11-03\r\n";

    private final ExcelToCsvBinaryStreamerTransformer transformer = new ExcelToCsvBinaryStreamerTransformer();

    @Test
    public void testCanTransformExcelToCsv() {
        assertTrue(transformer.canTransform(new TestBinaryStreamer("data.xlsx", new byte[0]), "csv"));
        assertTrue(transformer.canTransform(new TestBinaryStreamer("data.xls", new byte[0]), "csv"));
        assertTrue(transformer.canTransform(new TestBinaryStreamer("DATA.XLSX", new byte[0]), "CSV"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(new TestBinaryStreamer("data.csv", new byte[0]), "csv"));
        assertFalse(transformer.canTransform(new TestBinaryStreamer("data.ods", new byte[0]), "csv"));
        assertFalse(transformer.canTransform(new TestBinaryStreamer("data.xlsx", new byte[0]), "pdf"));
        assertFalse(transformer.canTransform(new TestBinaryStreamer("data", new byte[0]), "csv"));
    }

    @Test
    public void testTransformXlsx() throws IOException {
        assertEquals(EXPECTED_CSV, transform("data.xlsx", sampleWorkbook(new XSSFWorkbook())));
    }

    @Test
    public void testTransformXls() throws IOException {
        assertEquals(EXPECTED_CSV, transform("data.xls", sampleWorkbook(new HSSFWorkbook())));
    }

    @Test
    public void testTransformWorkbookWithEmptySheet() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Empty");
        assertEquals("", transform("data.xlsx", workbook));
    }

    @Test
    public void testTransformInvalidContentFails() {
        TestBinaryStreamer source = new TestBinaryStreamer("data.xlsx", "not excel".getBytes(UTF_8));

        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "csv")));
    }

    @Test
    public void testRoundTripWithCsvToExcel() throws IOException {
        String csv = "name,city\r\nȘtefan,\"Cluj, RO\"\r\n";
        byte[] xlsx = readAll(new CsvToExcelBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("data.csv", csv.getBytes(UTF_8)), "xlsx"));

        byte[] result = readAll(transformer.transformStreamer(new TestBinaryStreamer("data.xlsx", xlsx), "csv"));

        assertEquals(csv, new String(result, UTF_8));
    }

    /**
     * A first sheet with text, numbers, a formula, a date and a missing row and cell,
     * and a second sheet that is not converted.
     */
    private Workbook sampleWorkbook(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Data");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("name");
        header.createCell(1).setCellValue("qty");
        header.createCell(2).setCellValue("note");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Ștefan, Ion");
        row.createCell(1).setCellValue(3);
        row.createCell(2).setCellValue("say \"hi\"");

        Row computed = sheet.createRow(3);
        computed.createCell(1).setCellFormula("B2*2");
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
        computed.createCell(2).setCellValue(LocalDate.of(2026, 11, 3));
        computed.getCell(2).setCellStyle(dateStyle);

        workbook.createSheet("Other").createRow(0).createCell(0).setCellValue("ignored");
        return workbook;
    }

    private String transform(String name, Workbook workbook) throws IOException {
        ByteArrayOutputStream excel = new ByteArrayOutputStream();
        try (workbook) {
            workbook.write(excel);
        }
        byte[] csv = readAll(transformer.transformStreamer(new TestBinaryStreamer(name, excel.toByteArray()), "csv"));
        return new String(csv, UTF_8);
    }
}
