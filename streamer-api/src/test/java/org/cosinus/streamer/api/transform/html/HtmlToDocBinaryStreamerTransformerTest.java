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

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.cosinus.streamer.api.transform.doc.RtfWordReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HtmlToDocBinaryStreamerTransformerTest {

    private static final String HTML = """
        <!DOCTYPE html>
        <html><head><meta charset="utf-8"><title>Not in the document</title></head>
        <body>
        <h1>Introducere</h1>
        <p>Ștefan locuiește în Iași.</p>
        <p align="center">Centered text</p>
        <ol><li>first<ul><li>nested</ul><li>second</ol>
        <table border="1"><tr><th>name<th>qty<tr><td>apples<td>12</table>
        <p>After the table</p>
        <p style="page-break-before: always">Second page</p>
        </body></html>
        """;

    private static final List<String> PARAGRAPHS = List.of(
        "Introducere",
        "Ștefan locuiește în Iași.",
        "Centered text",
        "1. first",
        "    • nested",
        "2. second",
        "After the table",
        "Second page");

    private final HtmlToDocBinaryStreamerTransformer transformer = new HtmlToDocBinaryStreamerTransformer();

    @Test
    public void testCanTransformHtmlToWord() {
        assertTrue(transformer.canTransform(file("page.html"), "docx"));
        assertTrue(transformer.canTransform(file("page.htm"), "doc"));
        assertTrue(transformer.canTransform(file("PAGE.HTML"), "DOCX"));
        assertTrue(transformer.canTransform(file("PAGE.HTM"), "DOC"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("page.html"), "pdf"));
        assertFalse(transformer.canTransform(file("page.html"), "rtf"));
        assertFalse(transformer.canTransform(file("page.html"), null));
        assertFalse(transformer.canTransform(file("page.xhtml"), "docx"));
        assertFalse(transformer.canTransform(file("page.txt"), "docx"));
        assertFalse(transformer.canTransform(file("page"), "docx"));
        assertFalse(transformer.canTransform(image("page.html", new byte[0]), "docx"));
    }

    @Test
    public void testTransformToDocx() throws IOException {
        try (XWPFDocument word = docx(HTML)) {
            List<XWPFParagraph> paragraphs = word.getParagraphs();
            assertEquals(PARAGRAPHS, paragraphs.stream().map(XWPFParagraph::getText).toList());
            assertEquals(ParagraphAlignment.CENTER, paragraphs.get(2).getAlignment());
            // the heading is bold
            assertTrue(paragraphs.getFirst().getRuns().getFirst().isBold());
            assertFalse(paragraphs.get(6).isPageBreak());
            assertTrue(paragraphs.get(7).isPageBreak());

            assertEquals(1, word.getTables().size());
            assertEquals(List.of(List.of("name", "qty"), List.of("apples", "12")), rows(word.getTables().getFirst()));
        }
    }

    /**
     * The doc format is written as rtf, which word opens as a word document.
     */
    @Test
    public void testTransformToDoc() throws IOException {
        byte[] doc = transform(HTML, "doc");
        assertTrue(new String(doc, US_ASCII).startsWith("{\\rtf1"));

        List<DocxElement> elements = RtfWordReader.read(new ByteArrayInputStream(doc));
        assertEquals(PARAGRAPHS, elements.stream()
            .filter(DocxParagraph.class::isInstance)
            .map(element -> ((DocxParagraph) element).text())
            .toList());
        assertEquals(List.of(List.of("name", "qty"), List.of("apples", "12")), elements.stream()
            .filter(DocxTable.class::isInstance)
            .map(element -> ((DocxTable) element).rows())
            .findFirst()
            .orElseThrow());
        assertTrue(((DocxParagraph) elements.getLast()).pageBreakBefore());
    }

    @Test
    public void testEmptyHtmlGivesAnEmptyDocument() throws IOException {
        try (XWPFDocument word = docx("")) {
            assertTrue(word.getParagraphs().stream().allMatch(paragraph -> paragraph.getText().isEmpty()));
            assertTrue(word.getTables().isEmpty());
        }
        assertTrue(RtfWordReader.read(new ByteArrayInputStream(transform("", "doc"))).isEmpty());
    }

    private List<List<String>> rows(XWPFTable table) {
        return table.getRows().stream()
            .map(XWPFTableRow::getTableCells)
            .map(cells -> cells.stream().map(XWPFTableCell::getText).toList())
            .toList();
    }

    private XWPFDocument docx(String html) throws IOException {
        return new XWPFDocument(new ByteArrayInputStream(transform(html, "docx")));
    }

    private byte[] transform(String html, String format) {
        return readAll(transformer.transformStreamer(new TestBinaryStreamer("page.html", html.getBytes(UTF_8)), format));
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
