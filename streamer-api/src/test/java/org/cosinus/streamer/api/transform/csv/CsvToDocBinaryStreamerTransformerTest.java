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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
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

public class CsvToDocBinaryStreamerTransformerTest {

    private static final String CSV = "client;oraș;total\n\"Ionescu; SRL\";Cluj;1,350.00\nȘtefan {x} \\ y;Iași;-3.5\n";

    private static final List<List<String>> ROWS = List.of(
        List.of("client", "oraș", "total"),
        List.of("Ionescu; SRL", "Cluj", "1,350.00"),
        List.of("Ștefan {x} \\ y", "Iași", "-3.5"));

    private final CsvToDocBinaryStreamerTransformer transformer = new CsvToDocBinaryStreamerTransformer();

    @Test
    public void testCanTransformCsvToWord() {
        assertTrue(transformer.canTransform(csv("data.csv", ""), "docx"));
        assertTrue(transformer.canTransform(csv("data.csv", ""), "doc"));
        assertTrue(transformer.canTransform(csv("DATA.CSV", ""), "DOCX"));
        assertTrue(transformer.canTransform(csv("DATA.CSV", ""), "DOC"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(csv("data.csv", ""), "pdf"));
        assertFalse(transformer.canTransform(csv("data.csv", ""), "xlsx"));
        assertFalse(transformer.canTransform(csv("data.csv", ""), "rtf"));
        assertFalse(transformer.canTransform(csv("data.csv", ""), null));
        assertFalse(transformer.canTransform(csv("data.txt", ""), "docx"));
        assertFalse(transformer.canTransform(csv("data.xlsx", ""), "docx"));
        assertFalse(transformer.canTransform(image("data.csv", new byte[0]), "docx"));
    }

    @Test
    public void testTransformToDocx() throws IOException {
        try (XWPFDocument word = docx(CSV)) {
            assertEquals(1, word.getTables().size());
            assertEquals(ROWS, rows(word.getTables().getFirst()));
        }
    }

    /**
     * The doc format is written as rtf, which word opens as a word document.
     */
    @Test
    public void testTransformToDoc() throws IOException {
        byte[] doc = transform(CSV, "doc");
        String rtf = new String(doc, US_ASCII);
        assertTrue(rtf.startsWith("{\\rtf1"));
        assertTrue(rtf.endsWith("}"));

        List<DocxElement> elements = RtfWordReader.read(new ByteArrayInputStream(doc));
        assertEquals(1, elements.size());
        assertEquals(ROWS, ((DocxTable) elements.getFirst()).rows());
    }

    /**
     * The doc written is read back as a word document, e.g. when converted to pdf.
     */
    @Test
    public void testDocCanBeConvertedToPdf() throws IOException {
        byte[] doc = transform(CSV, "doc");
        byte[] pdf = readAll(new DocToPdfBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("facturi.doc", doc), "pdf"));
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("Ionescu; SRL"));
            assertTrue(text.contains("1,350.00"));
        }
    }

    @Test
    public void testRowsWithDifferentLengths() throws IOException {
        String csv = "a,b,c\nd\ne,f\n";
        List<List<String>> expected = List.of(List.of("a", "b", "c"), List.of("d", "", ""), List.of("e", "f", ""));

        try (XWPFDocument word = docx(csv)) {
            assertEquals(expected, rows(word.getTables().getFirst()));
        }
        List<DocxElement> elements = RtfWordReader.read(new ByteArrayInputStream(transform(csv, "doc")));
        assertEquals(expected, ((DocxTable) elements.getFirst()).rows());
    }

    @Test
    public void testMultilineValue() throws IOException {
        try (XWPFDocument word = docx("note\n\"first\nsecond\"\n")) {
            assertEquals("first\nsecond", word.getTables().getFirst().getRow(1).getCell(0).getText());
        }
    }

    @Test
    public void testEmptyCsvGivesAnEmptyDocument() throws IOException {
        try (XWPFDocument word = docx("")) {
            assertTrue(word.getTables().isEmpty());
        }
        assertTrue(RtfWordReader.read(new ByteArrayInputStream(transform("", "doc"))).isEmpty());
    }

    private List<List<String>> rows(XWPFTable table) {
        return table.getRows().stream()
            .map(row -> row.getTableCells().stream().map(XWPFTableCell::getText).toList())
            .toList();
    }

    private XWPFDocument docx(String csv) throws IOException {
        return new XWPFDocument(new ByteArrayInputStream(transform(csv, "docx")));
    }

    private byte[] transform(String csv, String format) {
        return readAll(transformer.transformStreamer(csv("data.csv", csv), format));
    }

    private TestBinaryStreamer csv(String name, String content) {
        return new TestBinaryStreamer(name, content.getBytes(UTF_8));
    }
}
