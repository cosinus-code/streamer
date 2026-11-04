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

public class RtfWordWriterTest {

    @Test
    public void testText() {
        assertEquals("plain", RtfWordWriter.text("plain"));
        assertEquals("\\\\ \\{ \\}", RtfWordWriter.text("\\ { }"));
        assertEquals("a\\line b\\line c\\tab d", RtfWordWriter.text("a\nb\r\nc\td"));
        assertEquals("\\u536?tefan \\u259?", RtfWordWriter.text("Ștefan ă"));
        // the values over 32767 are negative
        assertEquals("\\u-1?", RtfWordWriter.text("￿"));
        assertEquals("ab", RtfWordWriter.text("a\u0001b"));
        assertEquals("", RtfWordWriter.text(null));
    }

    /**
     * The rtf written is read back with the same elements.
     */
    @Test
    public void testRoundTrip() throws IOException {
        List<DocxElement> elements = List.of(
            new DocxParagraph("Title", "heading1", 20f, Alignment.CENTER, null, 0, null, false),
            new DocxParagraph("Ștefan {first}\nsecond line", "", 11f, Alignment.RIGHT, null, 0, null, false),
            new DocxTable(List.of(List.of("name", "qty"), List.of("mere", "12"))),
            new DocxParagraph("new page", "", null, Alignment.LEFT, null, 0, null, true));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new RtfWordWriter(elements).write(output);
        List<DocxElement> read = RtfWordReader.read(new ByteArrayInputStream(output.toByteArray()));

        DocxParagraph title = (DocxParagraph) read.get(0);
        assertEquals("Title", title.text());
        assertEquals(Alignment.CENTER, title.alignment());
        assertEquals(20, title.fontSize(), 0.01);

        DocxParagraph text = (DocxParagraph) read.get(1);
        assertEquals("Ștefan {first}\nsecond line", text.text());
        assertEquals(Alignment.RIGHT, text.alignment());
        assertFalse(text.pageBreakBefore());

        assertEquals(List.of(List.of("name", "qty"), List.of("mere", "12")), ((DocxTable) read.get(2)).rows());

        DocxParagraph newPage = (DocxParagraph) read.getLast();
        assertEquals("new page", newPage.text());
        assertTrue(newPage.pageBreakBefore());
    }
}
