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
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.WordToPdfBinaryStreamer.ListNumbering;
import org.cosinus.streamer.api.transform.word.WordElement.Alignment;
import org.cosinus.streamer.api.transform.word.WordElement.WordParagraph;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.pdf.PdfTextWriter.FONT_SIZE;
import static org.cosinus.streamer.api.transform.pdf.PdfTextWriter.PAGE_SIZE;
import static org.cosinus.streamer.api.transform.pdf.WordToPdfBinaryStreamer.TITLE_FONT_SIZE;
import static org.cosinus.streamer.api.transform.pdf.WordToPdfBinaryStreamer.fontSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class WordToPdfBinaryStreamerTransformerTest {

    private final WordToPdfBinaryStreamerTransformer transformer = new WordToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformWordToPdf() {
        assertTrue(transformer.canTransform(file("letter.docx"), "pdf"));
        assertTrue(transformer.canTransform(file("LETTER.DOCX"), "PDF"));
        assertTrue(transformer.canTransform(file("letter.doc"), "pdf"));
        assertTrue(transformer.canTransform(file("LETTER.DOC"), "PDF"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("letter.odt"), "pdf"));
        assertFalse(transformer.canTransform(file("data.xlsx"), "pdf"));
        assertFalse(transformer.canTransform(file("letter"), "pdf"));
        assertFalse(transformer.canTransform(file("letter.docx"), "csv"));
        assertFalse(transformer.canTransform(image("letter.docx", new byte[0]), "pdf"));
    }

    @Test
    public void testTransformDocx() throws IOException {
        XWPFDocument word = new XWPFDocument();
        paragraph(word, "Raport anual").setStyle("Title");
        paragraph(word, "Introducere").setStyle("Heading1");
        paragraph(word, "Ștefan locuiește în Iași.");
        paragraph(word, "Centered text").setAlignment(ParagraphAlignment.CENTER);
        BigInteger list = numberedList(word);
        paragraph(word, "first item").setNumID(list);
        paragraph(word, "second item").setNumID(list);
        XWPFTable table = word.createTable(2, 2);
        table.getRow(0).getCell(0).setText("name");
        table.getRow(0).getCell(1).setText("qty");
        table.getRow(1).getCell(0).setText("apples");
        table.getRow(1).getCell(1).setText("12");
        paragraph(word, "After the table");

        try (PDDocument pdf = transform(word)) {
            assertEquals(1, pdf.getNumberOfPages());
            String text = text(pdf);
            assertTrue(text.contains("Raport anual"));
            assertTrue(text.contains("Introducere"));
            // with the fallback font, the diacritics are replaced by their base letter
            assertTrue(text.contains("Ștefan locuiește în Iași.") || text.contains("Stefan locuieste in Iasi."));
            assertTrue(text.contains("Centered text"));
            assertTrue(text.contains("1. first item") || text.contains("1.first item"));
            assertTrue(text.contains("2. second item") || text.contains("2.second item"));
            assertTrue(text.contains("name"));
            assertTrue(text.contains("apples"));
            assertTrue(text.contains("12"));
            assertTrue(text.indexOf("apples") < text.indexOf("After the table"));
        }
    }

    @Test
    public void testTransformDoc() throws IOException {
        try (PDDocument pdf = transform("sample.doc", resource("sample.doc"))) {
            assertEquals(2, pdf.getNumberOfPages());
            String firstPage = pageText(pdf, 1);
            assertTrue(firstPage.contains("Introducere"));
            // with the fallback font, the diacritics are replaced by their base letter
            assertTrue(firstPage.contains("Ștefan locuiește în Iași.") || firstPage.contains("Stefan locuieste in Iasi."));
            assertTrue(firstPage.contains("Centered text"));
            assertTrue(firstPage.matches("(?s).*1\\.\\s*first item.*"));
            assertTrue(firstPage.matches("(?s).*2\\.\\s*second item.*"));
            assertTrue(firstPage.matches("(?s).*\u2022\\s*bullet item.*"));
            assertTrue(firstPage.contains("name"));
            assertTrue(firstPage.contains("apples"));
            assertTrue(firstPage.indexOf("apples") < firstPage.indexOf("After the table"));
            assertFalse(firstPage.contains("Second page"));
            assertTrue(pageText(pdf, 2).contains("Second page"));
        }
    }

    @Test
    public void testFormatIsDetectedFromContent() throws IOException {
        try (PDDocument pdf = transform("misnamed.docx", resource("sample.doc"))) {
            assertTrue(text(pdf).contains("Introducere"));
        }
    }

    @Test
    public void testPagesArePortraitA4() throws IOException {
        XWPFDocument word = new XWPFDocument();
        paragraph(word, "text");
        try (PDDocument pdf = transform(word)) {
            assertEquals(PAGE_SIZE.getWidth(), pdf.getPage(0).getMediaBox().getWidth(), 0.01);
            assertEquals(PAGE_SIZE.getHeight(), pdf.getPage(0).getMediaBox().getHeight(), 0.01);
            assertTrue(PAGE_SIZE.getWidth() < PAGE_SIZE.getHeight());
        }
    }

    @Test
    public void testLongTextIsWrappedAndContinuesOnNextPages() throws IOException {
        XWPFDocument word = new XWPFDocument();
        String words = IntStream.range(0, 2000)
            .mapToObj(index -> "word" + index)
            .collect(Collectors.joining(" "));
        paragraph(word, words);

        try (PDDocument pdf = transform(word)) {
            assertTrue(pdf.getNumberOfPages() > 1);
            String text = text(pdf);
            assertTrue(text.contains("word0 "));
            assertTrue(text.contains("word1999"));
            assertTrue(text.lines().count() > 100);
        }
    }

    @Test
    public void testPageBreakBeforeParagraph() throws IOException {
        XWPFDocument word = new XWPFDocument();
        XWPFParagraph first = paragraph(word, "first page");
        first.setPageBreak(true);
        paragraph(word, "second page").setPageBreak(true);

        try (PDDocument pdf = transform(word)) {
            assertEquals(2, pdf.getNumberOfPages());
            assertTrue(pageText(pdf, 1).contains("first page"));
            assertTrue(pageText(pdf, 2).contains("second page"));
        }
    }

    @Test
    public void testEmptyDocumentGivesAPage() throws IOException {
        try (PDDocument pdf = transform(new XWPFDocument())) {
            assertEquals(1, pdf.getNumberOfPages());
        }
    }

    @Test
    public void testInvalidContentFails() throws IOException {
        assertTransformFails("letter.docx", "not a word document".getBytes(UTF_8));
        assertTransformFails("letter.doc", "not a word document".getBytes(UTF_8));

        ByteArrayOutputStream excel = new ByteArrayOutputStream();
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            workbook.createSheet("Data");
            workbook.write(excel);
        }
        assertTransformFails("letter.doc", excel.toByteArray());
    }

    private void assertTransformFails(String name, byte[] content) {
        TestBinaryStreamer source = new TestBinaryStreamer(name, content);
        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "pdf")));
    }

    @Test
    public void testFontSize() {
        assertEquals(TITLE_FONT_SIZE, fontSize(paragraph("title", null)), 0.01);
        assertTrue(fontSize(paragraph("heading1", null)) > fontSize(paragraph("heading2", null)));
        assertTrue(fontSize(paragraph("heading2", null)) > FONT_SIZE);
        assertEquals(FONT_SIZE, fontSize(paragraph("heading9", null)), 0.01);
        assertEquals(14, fontSize(paragraph("", 14f)), 0.01);
        assertEquals(36, fontSize(paragraph("", 72f)), 0.01);
        assertEquals(FONT_SIZE, fontSize(paragraph("", null)), 0.01);
    }

    @Test
    public void testListNumbering() {
        ListNumbering numbering = new ListNumbering();
        String list = "one";
        assertEquals("1.", numbering.next(list, 0, "decimal"));
        assertEquals("a.", numbering.next(list, 1, "lowerLetter"));
        assertEquals("b.", numbering.next(list, 1, "lowerLetter"));
        assertEquals("2.", numbering.next(list, 0, "decimal"));
        assertEquals("a.", numbering.next(list, 1, "lowerLetter"));
        assertEquals("•", numbering.next("two", 0, "bullet"));
        assertEquals("•", numbering.next("two", 0, null));
        assertEquals("3.", numbering.next(list, 0, "decimal"));

        String roman = "roman";
        assertEquals("i.", numbering.next(roman, 0, "lowerRoman"));
        assertEquals("ii.", numbering.next(roman, 0, "lowerRoman"));
        assertEquals("iii.", numbering.next(roman, 0, "lowerRoman"));
        assertEquals("IV.", numbering.next(roman, 0, "upperRoman"));
        assertEquals("A.", numbering.next("letters", 0, "upperLetter"));
        assertEquals("", numbering.next("none", 0, "none"));
    }

    @Test
    public void testWrapBreaksLongWords() throws IOException {
        try (PDDocument pdf = new PDDocument()) {
            PdfTextFont font = PdfTextFont.helvetica();
            PdfTextWriter writer = new PdfTextWriter(pdf, font);
            float width = font.width("abcde", FONT_SIZE);

            List<String> lines = writer.wrap("ab cd abcdefghij\n\nend", FONT_SIZE, width);
            assertEquals(List.of("ab cd", "abcde", "fghij", "", "end"), lines);
            lines.forEach(line -> {
                try {
                    assertTrue(font.width(line, FONT_SIZE) <= width);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private XWPFParagraph paragraph(XWPFDocument word, String text) {
        XWPFParagraph paragraph = word.createParagraph();
        paragraph.createRun().setText(text);
        return paragraph;
    }

    private BigInteger numberedList(XWPFDocument word) {
        CTAbstractNum abstractNum = CTAbstractNum.Factory.newInstance();
        abstractNum.setAbstractNumId(BigInteger.ZERO);
        CTLvl level = abstractNum.addNewLvl();
        level.setIlvl(BigInteger.ZERO);
        level.addNewNumFmt().setVal(STNumberFormat.DECIMAL);
        level.addNewLvlText().setVal("%1.");

        XWPFNumbering numbering = word.createNumbering();
        BigInteger abstractNumId = numbering.addAbstractNum(new XWPFAbstractNum(abstractNum));
        return numbering.addNum(abstractNumId);
    }

    private PDDocument transform(XWPFDocument word) throws IOException {
        ByteArrayOutputStream docx = new ByteArrayOutputStream();
        try (word) {
            word.write(docx);
        }
        return transform("letter.docx", docx.toByteArray());
    }

    private PDDocument transform(String name, byte[] content) throws IOException {
        byte[] pdf = readAll(transformer.transformStreamer(new TestBinaryStreamer(name, content), "pdf"));
        return Loader.loadPDF(pdf);
    }

    private byte[] resource(String name) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(name)) {
            assertNotNull(name, input);
            return input.readAllBytes();
        }
    }

    private WordParagraph paragraph(String style, Float fontSize) {
        return new WordParagraph("text", style, fontSize, Alignment.LEFT, null, 0, null, false);
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
