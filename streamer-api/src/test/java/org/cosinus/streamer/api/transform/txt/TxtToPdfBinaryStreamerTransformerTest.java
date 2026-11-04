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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.pdf.PdfPlainTextWriter.PAGE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TxtToPdfBinaryStreamerTransformerTest {

    private final TxtToPdfBinaryStreamerTransformer transformer = new TxtToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformTxtToPdf() {
        assertTrue(transformer.canTransform(file("notes.txt"), "pdf"));
        assertTrue(transformer.canTransform(file("NOTES.TXT"), "PDF"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("notes.txt"), "docx"));
        assertFalse(transformer.canTransform(file("notes.txt"), "png"));
        assertFalse(transformer.canTransform(file("data.csv"), "pdf"));
        assertFalse(transformer.canTransform(file("notes"), "pdf"));
        assertFalse(transformer.canTransform(image("notes.txt", new byte[0]), "pdf"));
    }

    @Test
    public void testTransformTxt() throws IOException {
        try (PDDocument pdf = transform("Colinde de Crăciun\n\nȘtefan din Iași\n{braces} \\ backslash\n")) {
            assertEquals(1, pdf.getNumberOfPages());
            assertEquals(PAGE_SIZE.getWidth(), pdf.getPage(0).getMediaBox().getWidth(), 0.01);
            String text = new PDFTextStripper().getText(pdf);
            assertTrue(text.contains("Colinde de Cr"));
            // with the fallback font, the diacritics are replaced by their base letter
            assertTrue(text.contains("Ștefan din Iași") || text.contains("Stefan din Iasi"));
            assertTrue(text.contains("{braces} \\ backslash"));
        }
    }

    /**
     * The text aligned with tabs or spaces stays aligned, as all the characters have the same width.
     */
    @Test
    public void testColumnsStayAligned() throws IOException {
        try (PDDocument pdf = transform("A\tB1\nAAAA\tB2\nWWWWWWW B3\niiiiiii B4\n")) {
            Map<String, Float> x = wordPositions(pdf);
            assertEquals(x.get("B1"), x.get("B2"), 0.01);
            assertEquals(x.get("B3"), x.get("B4"), 0.01);
        }
    }

    @Test
    public void testFormFeedStartsANewPage() throws IOException {
        try (PDDocument pdf = transform("\fpage one\n\fpage two\n")) {
            // no empty first page for the leading form feed
            assertEquals(2, pdf.getNumberOfPages());
            assertTrue(pageText(pdf, 1).contains("page one"));
            assertTrue(pageText(pdf, 2).contains("page two"));
        }
    }

    @Test
    public void testLongTextContinuesOnNextPages() throws IOException {
        String text = IntStream.range(0, 300).mapToObj(index -> "line " + index).collect(Collectors.joining("\n"));
        try (PDDocument pdf = transform(text)) {
            assertTrue(pdf.getNumberOfPages() > 1);
            String all = new PDFTextStripper().getText(pdf);
            assertTrue(all.contains("line 0"));
            assertTrue(all.contains("line 299"));
        }
    }

    @Test
    public void testEmptyTextGivesAPage() throws IOException {
        try (PDDocument pdf = transform("")) {
            assertEquals(1, pdf.getNumberOfPages());
        }
    }

    /**
     * @return the horizontal position of the first character of each word
     */
    private Map<String, Float> wordPositions(PDDocument pdf) throws IOException {
        Map<String, Float> positions = new HashMap<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) {
                StringBuilder word = new StringBuilder();
                float start = 0;
                for (TextPosition position : textPositions) {
                    if (position.getUnicode().isBlank()) {
                        if (!word.isEmpty()) {
                            positions.put(word.toString(), start);
                            word.setLength(0);
                        }
                    } else {
                        if (word.isEmpty()) {
                            start = position.getXDirAdj();
                        }
                        word.append(position.getUnicode());
                    }
                }
                if (!word.isEmpty()) {
                    positions.put(word.toString(), start);
                }
            }
        };
        stripper.getText(pdf);
        return positions;
    }

    private String pageText(PDDocument pdf, int page) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getText(pdf);
    }

    private PDDocument transform(String text) throws IOException {
        return Loader.loadPDF(readAll(transformer.transformStreamer(
            new TestBinaryStreamer("notes.txt", text.getBytes(UTF_8)), "pdf")));
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
