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
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer;
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

public class PdfToTxtBinaryStreamerTransformerTest {

    private final PdfToTxtBinaryStreamerTransformer transformer = new PdfToTxtBinaryStreamerTransformer();

    @Test
    public void testCanTransformPdfToTxt() {
        assertTrue(transformer.canTransform(file("report.pdf"), "txt"));
        assertTrue(transformer.canTransform(file("REPORT.PDF"), "TXT"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("report.pdf"), "docx"));
        assertFalse(transformer.canTransform(file("report.pdf"), "pdf"));
        assertFalse(transformer.canTransform(file("report.docx"), "txt"));
        assertFalse(transformer.canTransform(file("notes.txt"), "txt"));
        assertFalse(transformer.canTransform(file("report"), "txt"));
        assertFalse(transformer.canTransform(image("report.pdf", new byte[0]), "txt"));
    }

    /**
     * The lines of a paragraph are joined in a line of text, and the next page starts with a form feed.
     */
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

        assertEquals("""
            Annual report
            This is the first line of a paragraph which continues here.
            Second paragraph with a hyphen-ated word.
            \fText of the next page.
            """, transform(pdf));
    }

    /**
     * A word document converted to pdf gives back its text.
     */
    @Test
    public void testRoundTripFromWord() throws IOException {
        byte[] doc;
        try (InputStream input = getClass().getResourceAsStream("/org/cosinus/streamer/api/transform/doc/sample.doc")) {
            assertNotNull(input);
            doc = input.readAllBytes();
        }
        byte[] pdf = readAll(new DocToPdfBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("sample.doc", doc), "pdf"));

        List<String> lines = transform(pdf).lines().toList();
        assertTrue(lines.contains("Introducere"));
        assertTrue(lines.contains("Centered text"));
        assertTrue(lines.contains("After the table"));
        assertTrue(lines.contains("\fSecond page"));
    }

    @Test
    public void testPdfWithoutTextGivesAnEmptyText() throws IOException {
        assertEquals("", transform(pdf(List.of())));
    }

    @Test
    public void testInvalidContentFails() {
        TestBinaryStreamer source = new TestBinaryStreamer("report.pdf", "not a pdf".getBytes(UTF_8));
        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "txt")));
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

    private String transform(byte[] pdf) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer("report.pdf", pdf), "txt")), UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
