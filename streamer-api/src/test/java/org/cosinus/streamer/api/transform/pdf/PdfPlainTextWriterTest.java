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
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.cosinus.streamer.api.transform.pdf.PdfPlainTextWriter.FONT_SIZE;
import static org.cosinus.streamer.api.transform.pdf.PdfPlainTextWriter.MARGIN;
import static org.cosinus.streamer.api.transform.pdf.PdfPlainTextWriter.PAGE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdfPlainTextWriterTest {

    @Test
    public void testExpandTabs() {
        assertEquals("no tab", PdfPlainTextWriter.expandTabs("no tab"));
        assertEquals("        x", PdfPlainTextWriter.expandTabs("\tx"));
        assertEquals("a       b", PdfPlainTextWriter.expandTabs("a\tb"));
        assertEquals("abcdefgh        i", PdfPlainTextWriter.expandTabs("abcdefgh\ti"));
        assertEquals("ș       ț", PdfPlainTextWriter.expandTabs("ș\tț"));
    }

    @Test
    public void testMonospaceFallback() throws IOException {
        PdfTextFont courier = PdfTextFont.courier();
        assertEquals(courier.width("i", FONT_SIZE), courier.width("W", FONT_SIZE), 0.001);
    }

    @Test
    public void testWrap() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfPlainTextWriter writer = new PdfPlainTextWriter(document, PdfTextFont.courier());
            int columns = columns();

            assertEquals(List.of("short line"), writer.wrap("short line"));
            assertEquals(List.of(""), writer.wrap(""));
            // wrapped after the last space that fits, the space itself is dropped
            String words = "word ".repeat(columns / 5) + "next";
            List<String> parts = writer.wrap(words);
            assertEquals(2, parts.size());
            assertTrue(parts.get(0).endsWith("word"));
            assertEquals("next", parts.get(1).strip());
            // a word longer than the line is cut
            parts = writer.wrap("x".repeat(columns + 5));
            assertEquals(List.of("x".repeat(columns), "x".repeat(5)), parts);
            // the indentation of a line is kept, not taken as a place to wrap
            parts = writer.wrap("    " + "y".repeat(columns));
            assertEquals("    " + "y".repeat(columns - 4), parts.get(0));
        }
    }

    @Test(timeout = 20_000)
    public void testVeryLongLineIsWrappedQuickly() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfPlainTextWriter writer = new PdfPlainTextWriter(document, PdfTextFont.courier());
            int columns = columns();
            List<String> parts = writer.wrap("z".repeat(columns * 5_000));
            assertEquals(5_000, parts.size());
        }
    }

    /**
     * @return the number of characters of the fallback monospaced font fitting a line
     */
    private int columns() throws IOException {
        float width = PdfTextFont.courier().width("x", FONT_SIZE);
        return (int) ((PAGE_SIZE.getWidth() - 2 * MARGIN) / width);
    }
}
