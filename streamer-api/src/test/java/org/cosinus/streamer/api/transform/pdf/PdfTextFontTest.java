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
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class PdfTextFontTest {

    @Test
    public void testSanitizeReplacesLineBreaksAndTabsBySpaces() {
        assertEquals("a b c d", PdfTextFont.helvetica().sanitize("a\r\nb\tc\nd"));
    }

    @Test
    public void testSanitizeNull() {
        assertEquals("", PdfTextFont.helvetica().sanitize(null));
    }

    @Test
    public void testHelveticaKeepsSupportedCharacters() {
        assertEquals("café, 12,50 €", PdfTextFont.helvetica().sanitize("café, 12,50 €"));
    }

    @Test
    public void testHelveticaReplacesUnsupportedDiacriticsByTheirBaseLetter() {
        assertEquals("Stefan si tara", PdfTextFont.helvetica().sanitize("Ștefan și țară"));
    }

    @Test
    public void testHelveticaReplacesOtherUnsupportedCharacters() {
        assertEquals("a ? b ?", PdfTextFont.helvetica().sanitize("a 😀 b 中"));
    }

    @Test
    public void testUnicodeFontKeepsDiacritics() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfTextFont font = PdfTextFont.load(document);
            assumeTrue("no unicode font installed", font.getFont() instanceof PDType0Font);

            assertEquals("Ștefan și țară", font.sanitize("Ștefan și țară"));
            assertEquals("unknown ?", font.sanitize("unknown \u0378"));
        }
    }

    @Test
    public void testWidth() throws IOException {
        PdfTextFont font = PdfTextFont.helvetica();

        assertEquals(0, font.width("", 10), 0);
        assertTrue(font.width("WWW", 10) > font.width("iii", 10));
        assertEquals(2 * font.width("abc", 10), font.width("abc", 20), 0.001);
    }
}
