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

package org.cosinus.streamer.api.transform.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.pdf.ExcelToPdfBinaryStreamer.PAGE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ExcelToPdfBinaryStreamerTransformerTest {

    private final ExcelToPdfBinaryStreamerTransformer transformer = new ExcelToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformExcelToPdf() {
        assertTrue(transformer.canTransform(file("data.xlsx"), "pdf"));
        assertTrue(transformer.canTransform(file("data.xls"), "pdf"));
        assertTrue(transformer.canTransform(file("DATA.XLSX"), "PDF"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("data.csv"), "pdf"));
        assertFalse(transformer.canTransform(file("data"), "pdf"));
        assertFalse(transformer.canTransform(file("data.xlsx"), "csv"));
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "pdf"));
    }

    @Test
    public void testTransformXlsx() throws IOException {
        assertSampleContent(transform("data.xlsx", sampleWorkbook(new XSSFWorkbook())));
    }

    @Test
    public void testTransformXls() throws IOException {
        assertSampleContent(transform("data.xls", sampleWorkbook(new HSSFWorkbook())));
    }

    @Test
    public void testEachVisibleSheetStartsOnANewPage() throws IOException {
        Workbook workbook = sampleWorkbook(new XSSFWorkbook());
        workbook.createSheet("Hidden").createRow(0).createCell(0).setCellValue("secret");
        workbook.setSheetHidden(2, true);

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            assertEquals(2, pdf.getNumberOfPages());
            assertTrue(pageText(pdf, 1).contains("Data"));
            assertTrue(pageText(pdf, 2).contains("Other"));
            assertTrue(pageText(pdf, 2).contains("second sheet"));
            assertFalse(text(pdf).contains("secret"));
        }
    }

    @Test
    public void testPagesAreLandscapeA4() throws IOException {
        try (PDDocument pdf = transform("data.xlsx", sampleWorkbook(new XSSFWorkbook()))) {
            assertEquals(PAGE_SIZE.getWidth(), pdf.getPage(0).getMediaBox().getWidth(), 0.01);
            assertEquals(PAGE_SIZE.getHeight(), pdf.getPage(0).getMediaBox().getHeight(), 0.01);
            assertTrue(PAGE_SIZE.getWidth() > PAGE_SIZE.getHeight());
        }
    }

    @Test
    public void testLongSheetContinuesOnNextPages() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Long");
        for (int index = 0; index < 200; index++) {
            sheet.createRow(index).createCell(0).setCellValue("row " + index);
        }

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            assertTrue(pdf.getNumberOfPages() > 1);
            String text = text(pdf);
            assertTrue(text.contains("row 0"));
            assertTrue(text.contains("row 199"));
            assertTrue(pageText(pdf, pdf.getNumberOfPages()).contains("Long"));
        }
    }

    @Test
    public void testWidestTextOfEachColumnIsNotTruncated() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Columns");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Nr.");
        header.createCell(1).setCellValue("Client");
        header.createCell(2).setCellValue("Preț unitar");
        for (int index = 1; index <= 25; index++) {
            Row row = sheet.createRow(index);
            row.createCell(0).setCellValue(index);
            row.createCell(1).setCellValue("Ștefan Ionescu SRL");
            row.createCell(2).setCellValue(12.5 * index);
        }

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            String text = text(pdf);
            assertTrue(text.contains("Nr."));
            assertTrue(text.contains("Preț unitar") || text.contains("Pret unitar"));
            assertTrue(text.contains("tefan Ionescu SRL"));
            assertFalse(text.contains("..."));
        }
    }

    @Test
    public void testWideSheetIsTruncatedToFitThePage() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Row row = workbook.createSheet("Wide").createRow(0);
        for (int column = 0; column < 30; column++) {
            row.createCell(column).setCellValue("a very long text in column number " + column);
        }

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            assertEquals(1, pdf.getNumberOfPages());
            String text = text(pdf);
            assertTrue(text.contains("..."));
            assertFalse(text.contains("a very long text in column number 29"));
        }
    }

    @Test
    public void testMultilineAndUnsupportedCharacters() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Row row = workbook.createSheet("Text").createRow(0);
        row.createCell(0).setCellValue("first line\nsecond\tline");
        // unassigned code point, without glyph in any font
        row.createCell(1).setCellValue("unknown \u0378");

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            String text = text(pdf);
            assertTrue(text.contains("first line second line"));
            assertTrue(text.contains("unknown ?"));
        }
    }

    @Test
    public void testWorkbookWithEmptySheet() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Empty");

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            assertEquals(1, pdf.getNumberOfPages());
            assertTrue(text(pdf).contains("Empty"));
        }
    }

    @Test
    public void testWorkbookWithOnlyHiddenSheetsGivesABlankPage() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        workbook.createSheet("Visible");
        workbook.createSheet("Hidden").createRow(0).createCell(0).setCellValue("secret");
        workbook.setActiveSheet(1);
        workbook.setSheetHidden(0, true);

        try (PDDocument pdf = transform("data.xlsx", workbook)) {
            assertEquals(1, pdf.getNumberOfPages());
            assertTrue(text(pdf).contains("secret"));
            assertFalse(text(pdf).contains("Visible"));
        }
    }

    @Test
    public void testTransformInvalidContentFails() {
        TestBinaryStreamer source = new TestBinaryStreamer("data.xlsx", "not excel".getBytes(UTF_8));

        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "pdf")));
    }

    private void assertSampleContent(PDDocument pdf) throws IOException {
        try (pdf) {
            String text = text(pdf);
            assertTrue(text.contains("Data"));
            assertTrue(text.contains("name"));
            assertTrue(text.contains("qty"));
            // with the fallback font, the diacritics are replaced by their base letter
            assertTrue(text.contains("Ștefan, Ion") || text.contains("Stefan, Ion"));
            assertTrue(text.contains("say \"hi\""));
            assertTrue(text.contains("6"));
            assertTrue(text.contains("2026-11-03"));
        }
    }

    /**
     * A first sheet with text, numbers, a formula, a date and a missing row and cell,
     * and a second sheet.
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

        workbook.createSheet("Other").createRow(0).createCell(0).setCellValue("second sheet");
        return workbook;
    }

    private PDDocument transform(String name, Workbook workbook) throws IOException {
        ByteArrayOutputStream excel = new ByteArrayOutputStream();
        try (workbook) {
            workbook.write(excel);
        }
        byte[] pdf = readAll(transformer.transformStreamer(new TestBinaryStreamer(name, excel.toByteArray()), "pdf"));
        return Loader.loadPDF(pdf);
    }

    private String text(PDDocument pdf) throws IOException {
        return new PDFTextStripper().getText(pdf);
    }

    private String pageText(PDDocument pdf, int page) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getText(pdf);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
