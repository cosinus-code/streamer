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

package org.cosinus.streamer.api.transform.txt;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.RtfWordReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.txt.TxtToDocBinaryStreamer.FALLBACK_CHARSET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TxtToDocBinaryStreamerTransformerTest {

    private static final String TEXT = "Colinde de Crăciun\n\n  Ștefan, Iași\tCluj\r\n{braces} \\ backslash\n";

    private static final List<String> LINES = List.of("Colinde de Crăciun", "", "  Ștefan, Iași\tCluj", "{braces} \\ backslash");

    private final TxtToDocBinaryStreamerTransformer transformer = new TxtToDocBinaryStreamerTransformer();

    @Test
    public void testCanTransformTxtToWord() {
        assertTrue(transformer.canTransform(file("notes.txt"), "docx"));
        assertTrue(transformer.canTransform(file("notes.txt"), "doc"));
        assertTrue(transformer.canTransform(file("NOTES.TXT"), "DOCX"));
        assertTrue(transformer.canTransform(file("NOTES.TXT"), "DOC"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("notes.txt"), "pdf"));
        assertFalse(transformer.canTransform(file("notes.txt"), "rtf"));
        assertFalse(transformer.canTransform(file("notes.txt"), null));
        assertFalse(transformer.canTransform(file("data.csv"), "docx"));
        assertFalse(transformer.canTransform(file("notes"), "docx"));
        assertFalse(transformer.canTransform(image("notes.txt", new byte[0]), "docx"));
    }

    /**
     * Each line is a paragraph, with its spaces and tabs, and the empty lines are kept.
     */
    @Test
    public void testTransformToDocx() throws IOException {
        try (XWPFDocument word = docx(TEXT.getBytes(UTF_8))) {
            assertEquals(LINES, word.getParagraphs().stream().map(XWPFParagraph::getText).toList());
        }
    }

    /**
     * The doc format is written as rtf, which word opens as a word document.
     */
    @Test
    public void testTransformToDoc() throws IOException {
        byte[] doc = transform(TEXT.getBytes(UTF_8), "doc");
        assertTrue(new String(doc, US_ASCII).startsWith("{\\rtf1"));

        List<DocxElement> elements = RtfWordReader.read(new ByteArrayInputStream(doc));
        assertEquals(LINES, elements.stream().map(element -> ((DocxParagraph) element).text()).toList());
    }

    @Test
    public void testFormFeedIsAPageBreak() throws IOException {
        try (XWPFDocument word = docx("page one\n\fpage two\nmore\fpage three\n".getBytes(UTF_8))) {
            List<XWPFParagraph> paragraphs = word.getParagraphs();
            assertEquals(List.of("page one", "page two", "more", "page three"),
                paragraphs.stream().map(XWPFParagraph::getText).toList());
            assertEquals(List.of(false, true, false, true),
                paragraphs.stream().map(XWPFParagraph::isPageBreak).toList());
        }
    }

    @Test
    public void testEmptyTextGivesAnEmptyDocument() throws IOException {
        try (XWPFDocument word = docx(new byte[0])) {
            assertTrue(word.getParagraphs().stream().allMatch(paragraph -> paragraph.getText().isEmpty()));
        }
        assertTrue(RtfWordReader.read(new ByteArrayInputStream(transform(new byte[0], "doc"))).isEmpty());
    }

    @Test
    public void testParagraphs() {
        assertEquals(List.of("a", "", "b"), texts("a\n\nb"));
        assertEquals(List.of("a", "b", "c"), texts("a\r\nb\rc\n"));
        assertEquals(List.of(""), texts("\n"));
        assertTrue(TxtToDocBinaryStreamer.paragraphs("").isEmpty());
    }

    @Test
    public void testDecode() {
        String text = "Ștefan în Iași";
        assertEquals(text, TxtToDocBinaryStreamer.decode(text.getBytes(UTF_8)));
        assertEquals(text, TxtToDocBinaryStreamer.decode(bytes(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
            text.getBytes(UTF_8))));
        assertEquals(text, TxtToDocBinaryStreamer.decode(bytes(new byte[]{(byte) 0xFE, (byte) 0xFF},
            text.getBytes(UTF_16BE))));
        assertEquals(text, TxtToDocBinaryStreamer.decode(bytes(new byte[]{(byte) 0xFF, (byte) 0xFE},
            text.getBytes(UTF_16LE))));

        // not valid utf-8: windows-1252
        String legacy = "café à la crème";
        assertEquals(legacy, TxtToDocBinaryStreamer.decode(legacy.getBytes(FALLBACK_CHARSET)));
    }

    private List<String> texts(String text) {
        return TxtToDocBinaryStreamer.paragraphs(text).stream()
            .map(element -> ((DocxParagraph) element).text())
            .toList();
    }

    private byte[] bytes(byte[] first, byte[] second) {
        byte[] bytes = new byte[first.length + second.length];
        System.arraycopy(first, 0, bytes, 0, first.length);
        System.arraycopy(second, 0, bytes, first.length, second.length);
        return bytes;
    }

    private XWPFDocument docx(byte[] text) throws IOException {
        return new XWPFDocument(new ByteArrayInputStream(transform(text, "docx")));
    }

    private byte[] transform(byte[] text, String format) {
        return readAll(transformer.transformStreamer(new TestBinaryStreamer("notes.txt", text), format));
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
