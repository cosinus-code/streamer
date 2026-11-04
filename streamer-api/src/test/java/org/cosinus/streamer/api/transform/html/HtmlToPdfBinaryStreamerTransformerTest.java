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

package org.cosinus.streamer.api.transform.html;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HtmlToPdfBinaryStreamerTransformerTest {

    private final HtmlToPdfBinaryStreamerTransformer transformer = new HtmlToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformHtmlToPdf() {
        assertTrue(transformer.canTransform(file("page.html"), "pdf"));
        assertTrue(transformer.canTransform(file("page.htm"), "pdf"));
        assertTrue(transformer.canTransform(file("PAGE.HTML"), "PDF"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("page.html"), "png"));
        assertFalse(transformer.canTransform(file("page.html"), "docx"));
        assertFalse(transformer.canTransform(file("page.xhtml"), "pdf"));
        assertFalse(transformer.canTransform(file("page.txt"), "pdf"));
        assertFalse(transformer.canTransform(file("page"), "pdf"));
        assertFalse(transformer.canTransform(image("page.html", new byte[0]), "pdf"));
    }

    @Test
    public void testTransformHtml() throws IOException {
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8"><title>Not in the pdf</title></head>
            <body>
            <h1>Introducere</h1>
            <p>Ștefan locuiește în Iași.</p>
            <p align="center">Centered text</p>
            <ol><li>first item<li>second item</ol>
            <ul><li>bullet item</ul>
            <table border="1"><tr><th>name<th>qty<tr><td>apples<td>12</table>
            <p>After the table</p>
            <p style="page-break-before: always">Second page</p>
            </body></html>
            """;
        try (PDDocument pdf = transform("page.html", html)) {
            assertEquals(2, pdf.getNumberOfPages());
            String firstPage = pageText(pdf, 1);
            assertTrue(firstPage.contains("Introducere"));
            assertFalse(firstPage.contains("Not in the pdf"));
            // with the fallback font, the diacritics are replaced by their base letter
            assertTrue(firstPage.contains("Ștefan locuiește în Iași.") || firstPage.contains("Stefan locuieste in Iasi."));
            assertTrue(firstPage.contains("Centered text"));
            assertTrue(firstPage.matches("(?s).*1\\.\\s*first item.*"));
            assertTrue(firstPage.matches("(?s).*2\\.\\s*second item.*"));
            assertTrue(firstPage.matches("(?s).*\u2022\\s*bullet item.*"));
            assertTrue(firstPage.matches("(?s).*name\\s+qty.*"));
            assertTrue(firstPage.matches("(?s).*apples\\s+12.*"));
            assertTrue(firstPage.indexOf("apples") < firstPage.indexOf("After the table"));
            assertFalse(firstPage.contains("Second page"));
            assertTrue(pageText(pdf, 2).contains("Second page"));
        }
    }

    @Test
    public void testEmptyHtmlGivesAPage() throws IOException {
        try (PDDocument pdf = transform("empty.html", "")) {
            assertEquals(1, pdf.getNumberOfPages());
        }
    }

    @Test
    public void testPdfNamedHtmlIsKeptAsItIs() throws IOException {
        byte[] pdf;
        try (PDDocument document = new PDDocument()) {
            document.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            document.save(output);
            pdf = output.toByteArray();
        }
        byte[] result = readAll(transformer.transformStreamer(new TestBinaryStreamer("page.html", pdf), "pdf"));
        org.junit.Assert.assertArrayEquals(pdf, result);
    }

    private PDDocument transform(String name, String html) throws IOException {
        byte[] pdf = readAll(transformer.transformStreamer(new TestBinaryStreamer(name, html.getBytes(UTF_8)), "pdf"));
        return Loader.loadPDF(pdf);
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
