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
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.csv.CsvToPdfBinaryStreamer.isNumber;
import static org.cosinus.streamer.api.transform.pdf.PdfTableWriter.PAGE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsvToPdfBinaryStreamerTransformerTest {

    private final CsvToPdfBinaryStreamerTransformer transformer = new CsvToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformCsvToPdf() {
        assertTrue(transformer.canTransform(csv("data.csv", ""), "pdf"));
        assertTrue(transformer.canTransform(csv("DATA.CSV", ""), "PDF"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(csv("data.txt", ""), "pdf"));
        assertFalse(transformer.canTransform(csv("data.xlsx", ""), "pdf"));
        assertFalse(transformer.canTransform(csv("data.csv", ""), "xlsx"));
        assertFalse(transformer.canTransform(image("data.csv", new byte[0]), "pdf"));
    }

    @Test
    public void testTransformCsv() throws IOException {
        try (PDDocument pdf = transform("facturi.csv",
            "\uFEFFclient;oraș;total\n\"Ionescu; SRL\";Cluj;1,350.00\nȘtefan;Iași;-3.5\n")) {

            assertEquals(1, pdf.getNumberOfPages());
            assertEquals(PAGE_SIZE.getWidth(), pdf.getPage(0).getMediaBox().getWidth(), 0.01);
            String text = text(pdf);
            assertTrue(text.contains("facturi.csv"));
            assertTrue(text.contains("client"));
            assertTrue(text.contains("Ionescu; SRL"));
            assertTrue(text.contains("1,350.00"));
            // with the fallback font, the diacritics are replaced by their base letter
            assertTrue(text.contains("Ștefan") || text.contains("Stefan"));
            assertTrue(text.contains("Iași") || text.contains("Iasi"));
            assertFalse(text.contains("\uFEFF"));
        }
    }

    @Test
    public void testLongCsvContinuesOnNextPages() throws IOException {
        String content = IntStream.range(0, 200)
            .mapToObj(index -> "row " + index + "," + index)
            .collect(Collectors.joining("\n"));

        try (PDDocument pdf = transform("long.csv", content)) {
            assertTrue(pdf.getNumberOfPages() > 1);
            String text = text(pdf);
            assertTrue(text.contains("row 0"));
            assertTrue(text.contains("row 199"));
        }
    }

    @Test
    public void testMultilineValue() throws IOException {
        try (PDDocument pdf = transform("notes.csv", "note\n\"first\nsecond\"\n")) {
            assertTrue(text(pdf).contains("first second"));
        }
    }

    @Test
    public void testEmptyCsvGivesAPageWithTheTitle() throws IOException {
        try (PDDocument pdf = transform("empty.csv", "")) {
            assertEquals(1, pdf.getNumberOfPages());
            assertTrue(text(pdf).contains("empty.csv"));
        }
    }

    @Test
    public void testIsNumber() {
        assertTrue(isNumber("12"));
        assertTrue(isNumber("-3.5"));
        assertTrue(isNumber("+7"));
        assertTrue(isNumber("1,350.00"));
        assertTrue(isNumber("12,50"));
        assertTrue(isNumber("25%"));
        assertTrue(isNumber(" 42 "));
        assertTrue(isNumber("007"));

        assertFalse(isNumber(null));
        assertFalse(isNumber(""));
        assertFalse(isNumber("-"));
        assertFalse(isNumber("abc"));
        assertFalse(isNumber("12 kg"));
        assertFalse(isNumber("2026-11-03"));
        assertFalse(isNumber(".5"));
    }

    private PDDocument transform(String name, String content) throws IOException {
        return Loader.loadPDF(readAll(transformer.transformStreamer(csv(name, content), "pdf")));
    }

    private String text(PDDocument pdf) throws IOException {
        return new PDFTextStripper().getText(pdf);
    }

    private TestBinaryStreamer csv(String name, String content) {
        return new TestBinaryStreamer(name, content.getBytes(UTF_8));
    }
}
