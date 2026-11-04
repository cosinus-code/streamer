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
import java.io.UncheckedIOException;
import java.util.List;

import static org.cosinus.streamer.api.transform.pdf.PdfTextWriter.FONT_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdfTextWriterTest {

    @Test
    public void testWrapBreaksLongWords() throws IOException {
        try (PDDocument pdf = new PDDocument()) {
            PdfTextFont font = PdfTextFont.helvetica();
            PdfTextWriter writer = new PdfTextWriter(pdf, font);
            float width = font.width("abcde", FONT_SIZE);

            List<String> lines = writer.wrap("ab cd abcdefghij\n\nend", FONT_SIZE, width);
            assertEquals(List.of("ab cd", "abcde", "fghij", "", "end"), lines);
            lines.forEach(line -> {
                try {
                    assertTrue(font.width(line, FONT_SIZE) <= width);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
