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

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DocxWordWriterTest {

    @Test
    public void testDocxWriter() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new DocxWordWriter(List.of(
            new DocxParagraph("Title", "title", 20f, Alignment.CENTER, null, 0, null, false),
            new DocxParagraph("first line\nsecond\u0001 line", "", null, Alignment.RIGHT, null, 0, null, true),
            new DocxTable(List.of(List.of("name", "qty"), List.of("apples"))),
            DocxParagraph.plain("end"))).write(output);

        try (XWPFDocument word = new XWPFDocument(new ByteArrayInputStream(output.toByteArray()))) {
            List<XWPFParagraph> paragraphs = word.getParagraphs();
            assertEquals(3, paragraphs.size());

            assertEquals("Title", paragraphs.get(0).getText());
            assertEquals(ParagraphAlignment.CENTER, paragraphs.get(0).getAlignment());
            assertTrue(paragraphs.get(0).getRuns().getFirst().isBold());
            assertEquals(20, paragraphs.get(0).getRuns().getFirst().getFontSizeAsDouble(), 0.01);

            assertEquals("first line\nsecond line", paragraphs.get(1).getText());
            assertEquals(ParagraphAlignment.RIGHT, paragraphs.get(1).getAlignment());
            assertTrue(paragraphs.get(1).isPageBreak());
            assertFalse(paragraphs.get(1).getRuns().getFirst().isBold());

            XWPFTable table = word.getTables().getFirst();
            assertEquals(2, table.getNumberOfRows());
            assertEquals("name", table.getRow(0).getCell(0).getText());
            assertEquals("qty", table.getRow(0).getCell(1).getText());
            assertEquals("apples", table.getRow(1).getCell(0).getText());
            assertEquals("", table.getRow(1).getCell(1).getText());

            assertEquals("end", paragraphs.get(2).getText());
        }
    }
}
