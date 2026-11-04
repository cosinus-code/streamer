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

package org.cosinus.streamer.api.transform.doc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.cosinus.streamer.api.transform.txt.TxtToDocBinaryStreamerTransformer;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class DocToTxtBinaryStreamerTransformerTest {

    private static final String SAMPLE_TEXT = """
        Introducere
        Ștefan locuiește în Iași.
        Centered text
        1. first item
        2. second item
        • bullet item
        name\tqty
        apples\t12
        After the table
        \fSecond page
        """;

    private final DocToTxtBinaryStreamerTransformer transformer = new DocToTxtBinaryStreamerTransformer();

    @Test
    public void testCanTransformWordToTxt() {
        assertTrue(transformer.canTransform(file("letter.docx"), "txt"));
        assertTrue(transformer.canTransform(file("letter.doc"), "txt"));
        assertTrue(transformer.canTransform(file("letter.rtf"), "txt"));
        assertTrue(transformer.canTransform(file("LETTER.DOCX"), "TXT"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("letter.docx"), "pdf"));
        assertFalse(transformer.canTransform(file("letter.docx"), "docx"));
        assertFalse(transformer.canTransform(file("letter.docx"), null));
        assertFalse(transformer.canTransform(file("notes.txt"), "txt"));
        assertFalse(transformer.canTransform(file("report.pdf"), "txt"));
        assertFalse(transformer.canTransform(file("letter"), "txt"));
        assertFalse(transformer.canTransform(image("letter.docx", new byte[0]), "txt"));
    }

    @Test
    public void testTransformDoc() throws IOException {
        assertEquals(SAMPLE_TEXT, transform("sample.doc", resource("sample.doc")));
    }

    /**
     * The numbers and bullets of the rtf lists are part of the text, with the tab following them.
     */
    @Test
    public void testTransformRtf() throws IOException {
        String text = transform("sample.rtf", resource("sample.rtf"));
        assertEquals(SAMPLE_TEXT.replace("1. first", " 1.\tfirst").replace("2. second", " 2.\tsecond")
            .replace("• bullet", "•\tbullet"), text);
    }

    @Test
    public void testTransformDocx() throws IOException {
        ByteArrayOutputStream docx = new ByteArrayOutputStream();
        try (XWPFDocument word = new XWPFDocument()) {
            word.createParagraph().createRun().setText("Title");
            XWPFRun lines = word.createParagraph().createRun();
            lines.setText("first line");
            lines.addBreak();
            lines.setText("second line");
            word.createParagraph();
            XWPFTable table = word.createTable(2, 2);
            table.getRow(0).getCell(0).setText("name");
            table.getRow(0).getCell(1).setText("qty");
            table.getRow(1).getCell(0).setText("mere");
            table.getRow(1).getCell(1).setText("12");
            XWPFParagraph nextPage = word.createParagraph();
            nextPage.setPageBreak(true);
            nextPage.createRun().setText("next page");
            word.write(docx);
        }

        assertEquals("Title\nfirst line\nsecond line\n\nname\tqty\nmere\t12\n\fnext page\n",
            transform("letter.docx", docx.toByteArray()));
    }

    @Test
    public void testListsAreNumberedAndIndented() {
        String text = TxtWordWriter.text(List.of(
            item("first", 0, "decimal"),
            item("nested\nsecond line", 1, "lowerLetter"),
            item("second", 0, "decimal"),
            item("no number", 0, "none")));
        assertEquals("""
            1. first
                a. nested
                   second line
            2. second
            no number
            """, text);
    }

    @Test
    public void testTableCellsStayOnTheirRow() {
        String text = TxtWordWriter.text(List.of(
            new DocxTable(List.of(List.of("multi\nline", "with\ttab", " spaces ")))));
        assertEquals("multi line\twith tab\tspaces\n", text);
    }

    /**
     * A page break before the first paragraph is not written, as it doesn't start a new page.
     */
    @Test
    public void testPageBreakBeforeTheFirstParagraphIsNotWritten() {
        String text = TxtWordWriter.text(List.of(
            new DocxParagraph("first", "", null, Alignment.LEFT, null, 0, null, true)));
        assertEquals("first\n", text);
    }

    /**
     * A pdf file saved with a doc extension (e.g. downloaded from the web) is read as pdf text.
     */
    @Test
    public void testPdfNamedDocIsReadAsPdf() throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(HELVETICA), 12);
                content.newLineAtOffset(56, 780);
                content.showText("Craciunul la romani");
                content.endText();
            }
            document.save(pdf);
        }
        assertEquals("Craciunul la romani\n", transform("craciunul la romani.doc", pdf.toByteArray()));
    }

    /**
     * The text written is read back by the text to word transformation, with the same lines and page breaks.
     */
    @Test
    public void testRoundTripThroughTxt() throws IOException {
        byte[] txt = SAMPLE_TEXT.getBytes(UTF_8);
        byte[] docx = readAll(new TxtToDocBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("notes.txt", txt), "docx"));
        assertEquals(SAMPLE_TEXT, transform("notes.docx", docx));
    }

    @Test
    public void testInvalidContentFails() {
        for (byte[] content : List.of("not a word document".getBytes(UTF_8), new byte[0])) {
            TestBinaryStreamer source = new TestBinaryStreamer("letter.doc", content);
            assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "txt")));
        }
    }

    private DocxParagraph item(String text, int level, String format) {
        return new DocxParagraph(text, "", null, Alignment.LEFT, "list", level, format, false);
    }

    private String transform(String name, byte[] content) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer(name, content), "txt")), UTF_8);
    }

    private byte[] resource(String name) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(name)) {
            assertNotNull(name, input);
            return input.readAllBytes();
        }
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
