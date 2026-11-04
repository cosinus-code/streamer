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
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.cosinus.streamer.api.transform.html.HtmlWordReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
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

public class DocToHtmlBinaryStreamerTransformerTest {

    private final DocToHtmlBinaryStreamerTransformer transformer = new DocToHtmlBinaryStreamerTransformer();

    @Test
    public void testCanTransformWordToHtml() {
        assertTrue(transformer.canTransform(file("letter.docx"), "html"));
        assertTrue(transformer.canTransform(file("letter.doc"), "htm"));
        assertTrue(transformer.canTransform(file("letter.rtf"), "html"));
        assertTrue(transformer.canTransform(file("LETTER.DOCX"), "HTML"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("letter.docx"), "pdf"));
        assertFalse(transformer.canTransform(file("letter.docx"), "xhtml"));
        assertFalse(transformer.canTransform(file("letter.docx"), null));
        assertFalse(transformer.canTransform(file("notes.txt"), "html"));
        assertFalse(transformer.canTransform(file("page.html"), "html"));
        assertFalse(transformer.canTransform(file("letter"), "html"));
        assertFalse(transformer.canTransform(image("letter.docx", new byte[0]), "html"));
    }

    @Test
    public void testTransformDoc() throws IOException {
        String html = transform("sample.doc", resource("sample.doc"));
        assertTrue(html.contains("<title>sample</title>"));
        assertTrue(html.contains("<h1"));
        assertTrue(html.contains("<li>first item</li>"));
        assertSample(html);
    }

    /**
     * The numbers and bullets of the rtf lists are part of the text.
     */
    @Test
    public void testTransformRtf() throws IOException {
        assertSample(transform("sample.rtf", resource("sample.rtf")));
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
        assertTrue(transform("craciunul.doc", pdf.toByteArray()).contains(">Craciunul la romani</p>"));
    }

    @Test
    public void testInvalidContentFails() {
        for (byte[] content : List.of("not a word document".getBytes(UTF_8), new byte[0])) {
            TestBinaryStreamer source = new TestBinaryStreamer("letter.doc", content);
            assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "html")));
        }
    }

    /**
     * The page is read back with the paragraphs, the table and the page break of the sample.
     */
    private void assertSample(String html) throws IOException {
        List<DocxElement> elements = HtmlWordReader.read(new ByteArrayInputStream(html.getBytes(UTF_8)));
        List<String> texts = elements.stream()
            .filter(DocxParagraph.class::isInstance)
            .map(element -> ((DocxParagraph) element).text().replaceFirst("^(\\d+\\.|•)\\s", ""))
            .toList();
        assertEquals(List.of("Introducere", "Ștefan locuiește în Iași.", "Centered text",
            "first item", "second item", "bullet item", "After the table", "Second page"), texts);
        assertEquals(List.of(List.of("name", "qty"), List.of("apples", "12")), elements.stream()
            .filter(DocxTable.class::isInstance)
            .map(element -> ((DocxTable) element).rows())
            .findFirst()
            .orElseThrow());
        DocxParagraph last = (DocxParagraph) elements.getLast();
        assertTrue(last.pageBreakBefore());
    }

    private String transform(String name, byte[] content) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer(name, content), "html")), UTF_8);
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
