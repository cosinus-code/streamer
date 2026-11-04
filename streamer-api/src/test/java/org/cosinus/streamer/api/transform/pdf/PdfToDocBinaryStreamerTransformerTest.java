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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.RtfWordReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
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

public class PdfToDocBinaryStreamerTransformerTest {

    private final PdfToDocBinaryStreamerTransformer transformer = new PdfToDocBinaryStreamerTransformer();

    @Test
    public void testCanTransformPdfToWord() {
        assertTrue(transformer.canTransform(file("report.pdf"), "docx"));
        assertTrue(transformer.canTransform(file("REPORT.PDF"), "DOCX"));
        assertTrue(transformer.canTransform(file("report.pdf"), "doc"));
        assertTrue(transformer.canTransform(file("REPORT.PDF"), "DOC"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("report.pdf"), "rtf"));
        assertFalse(transformer.canTransform(file("report.pdf"), null));
        assertFalse(transformer.canTransform(file("report.pdf"), "pdf"));
        assertFalse(transformer.canTransform(file("report.docx"), "docx"));
        assertFalse(transformer.canTransform(file("data.csv"), "docx"));
        assertFalse(transformer.canTransform(file("report"), "docx"));
        assertFalse(transformer.canTransform(image("report.pdf", new byte[0]), "docx"));
    }

    /**
     * The doc format is written as rtf, which word opens as a word document.
     */
    @Test
    public void testTransformPdfToDoc() throws IOException {
        byte[] pdf = pdf(
            List.of(
                new Line("Annual report", 20, 780),
                new Line("This is the first line of a", 11, 740),
                new Line("paragraph which continues here.", 11, 727),
                new Line("Second paragraph with a hyphen-", 11, 690),
                new Line("ated word.", 11, 677)),
            List.of(
                new Line("Text of the next page.", 11, 780)));

        byte[] doc = readAll(transformer.transformStreamer(new TestBinaryStreamer("report.pdf", pdf), "doc"));
        assertTrue(new String(doc, US_ASCII).startsWith("{\\rtf1"));

        List<DocxElement> elements = RtfWordReader.read(new ByteArrayInputStream(doc));
        List<DocxParagraph> paragraphs = elements.stream()
            .map(DocxParagraph.class::cast)
            .toList();
        assertEquals(List.of(
                "Annual report",
                "This is the first line of a paragraph which continues here.",
                "Second paragraph with a hyphen-ated word.",
                "Text of the next page."),
            paragraphs.stream().map(DocxParagraph::text).toList());
        assertEquals(20, paragraphs.getFirst().fontSize(), 0.01);
        assertEquals(11, paragraphs.get(1).fontSize(), 0.01);
        assertFalse(paragraphs.get(2).pageBreakBefore());
        assertTrue(paragraphs.get(3).pageBreakBefore());
    }

    @Test
    public void testTransformPdf() throws IOException {
        byte[] pdf = pdf(
            List.of(
                new Line("Annual report", 20, 780),
                new Line("This is the first line of a", 11, 740),
                new Line("paragraph which continues here.", 11, 727),
                new Line("Second paragraph with a hyphen-", 11, 690),
                new Line("ated word.", 11, 677)),
            List.of(
                new Line("Text of the next page.", 11, 780)));

        try (XWPFDocument word = transform(pdf)) {
            List<XWPFParagraph> paragraphs = word.getParagraphs();
            assertEquals(List.of(
                    "Annual report",
                    "This is the first line of a paragraph which continues here.",
                    "Second paragraph with a hyphen-ated word.",
                    "Text of the next page."),
                paragraphs.stream().map(XWPFParagraph::getText).toList());

            XWPFRun title = paragraphs.getFirst().getRuns().getFirst();
            assertTrue(title.isBold());
            assertEquals(20, title.getFontSizeAsDouble(), 0.01);
            XWPFRun body = paragraphs.get(1).getRuns().getFirst();
            assertFalse(body.isBold());
            assertEquals(11, body.getFontSizeAsDouble(), 0.01);

            assertFalse(paragraphs.get(0).isPageBreak());
            assertFalse(paragraphs.get(2).isPageBreak());
            assertTrue(paragraphs.get(3).isPageBreak());
        }
    }

    @Test
    public void testRoundTripFromWord() throws IOException {
        byte[] doc = resource("/org/cosinus/streamer/api/transform/doc/sample.doc");
        byte[] pdf = readAll(new DocToPdfBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("sample.doc", doc), "pdf"));

        try (XWPFDocument word = transform(pdf)) {
            List<String> texts = word.getParagraphs().stream().map(XWPFParagraph::getText).toList();
            assertTrue(texts.contains("Introducere"));
            assertTrue(texts.contains("Centered text"));
            assertTrue(texts.stream().anyMatch(text -> text.contains("first item")));
            assertTrue(texts.stream().anyMatch(text -> text.contains("apples")));

            XWPFParagraph heading = paragraph(word, "Introducere");
            assertTrue(heading.getRuns().getFirst().isBold());
            XWPFParagraph secondPage = paragraph(word, "Second page");
            assertTrue(secondPage.isPageBreak());
        }
    }

    @Test
    public void testPdfWithoutTextGivesAnEmptyDocument() throws IOException {
        try (XWPFDocument word = transform(pdf(List.of()))) {
            assertTrue(word.getParagraphs().stream().allMatch(paragraph -> paragraph.getText().isEmpty()));
        }
    }

    @Test
    public void testInvalidContentFails() {
        TestBinaryStreamer source = new TestBinaryStreamer("report.pdf", "not a pdf".getBytes(UTF_8));
        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "docx")));
    }

    private record Line(String text, float fontSize, float y) {
    }

    /**
     * @param pages the lines of each page
     * @return a pdf with the given lines, written with Helvetica from the left margin
     */
    @SafeVarargs
    private byte[] pdf(List<Line>... pages) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDType1Font font = new PDType1Font(HELVETICA);
            for (List<Line> lines : pages) {
                PDPage page = new PDPage(A4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    for (Line line : lines) {
                        content.beginText();
                        content.setFont(font, line.fontSize());
                        content.newLineAtOffset(56, line.y());
                        content.showText(line.text());
                        content.endText();
                    }
                }
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        }
    }

    private XWPFDocument transform(byte[] pdf) throws IOException {
        byte[] docx = readAll(transformer.transformStreamer(new TestBinaryStreamer("report.pdf", pdf), "docx"));
        return new XWPFDocument(new ByteArrayInputStream(docx));
    }

    private XWPFParagraph paragraph(XWPFDocument word, String text) {
        return word.getParagraphs().stream()
            .filter(paragraph -> paragraph.getText().equals(text))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing paragraph: " + text));
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
