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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RtfWordReaderTest {

    @Test
    public void testReadSample() throws IOException {
        List<DocxElement> elements;
        try (InputStream input = getClass().getResourceAsStream("sample.rtf")) {
            assertNotNull(input);
            elements = RtfWordReader.read(input);
        }

        DocxParagraph heading = (DocxParagraph) elements.get(0);
        assertEquals("Introducere", heading.text());
        assertEquals("heading1", heading.style());
        assertEquals(24, heading.fontSize(), 0.01);

        DocxParagraph body = (DocxParagraph) elements.get(1);
        assertEquals("Ștefan locuiește în Iași.", body.text());
        assertEquals(12, body.fontSize(), 0.01);
        assertEquals(Alignment.CENTER, ((DocxParagraph) elements.get(2)).alignment());

        assertTrue(((DocxParagraph) elements.get(3)).text().contains("first item"));

        DocxTable table = (DocxTable) elements.get(6);
        assertEquals(List.of(List.of("name", "qty"), List.of("apples", "12")), table.rows());

        DocxParagraph afterTable = (DocxParagraph) elements.get(7);
        assertEquals("After the table", afterTable.text());
        assertFalse(afterTable.pageBreakBefore());

        DocxParagraph secondPage = (DocxParagraph) elements.getLast();
        assertEquals("Second page", secondPage.text());
        assertTrue(secondPage.pageBreakBefore());
    }

    @Test
    public void testManualPageBreakSplitsTheParagraph() throws IOException {
        List<DocxElement> elements = read("{\\rtf1\\ansi before\\page after\\par}");
        assertEquals(2, elements.size());
        assertEquals("before", ((DocxParagraph) elements.get(0)).text());
        assertFalse(((DocxParagraph) elements.get(0)).pageBreakBefore());
        assertEquals("after", ((DocxParagraph) elements.get(1)).text());
        assertTrue(((DocxParagraph) elements.get(1)).pageBreakBefore());
    }

    @Test
    public void testUnicodeEscapes() throws IOException {
        // the default code page is windows-1252, where \'e3 is ã
        List<DocxElement> elements = read("{\\rtf1\\ansi\\uc1 \\u536?tefan \\u538?ar\\'e3\\par}");
        assertEquals("Ștefan Țarã", ((DocxParagraph) elements.getFirst()).text());
    }

    /**
     * Word on a romanian windows writes the diacritics in its code page, windows-1250.
     */
    @Test
    public void testCodePageEscapes() throws IOException {
        List<DocxElement> elements = read("{\\rtf1\\ansi\\ansicpg1250 \\'aatefan \\'eengheal\\'e3 \\'de\\'e3r\\'e3\\par}");
        assertEquals("Ştefan îngheală Ţără", ((DocxParagraph) elements.getFirst()).text());
    }

    /**
     * After a unicode escape, the code page character is only its replacement for older readers: it is skipped.
     */
    @Test
    public void testCodePageReplacementOfUnicodeEscapeIsSkipped() throws IOException {
        List<DocxElement> elements = read("{\\rtf1\\ansi\\ansicpg1250\\uc1 \\u259\\'e3 b\\u537\\'ba\\par}");
        assertEquals("ă bș", ((DocxParagraph) elements.getFirst()).text());
    }

    @Test
    public void testMarkTablesAndPageBreaks() {
        String rtf = "{\\rtf1{\\stylesheet{\\s1\\pagebb Heading;}}"
            + "\\trowd\\cellx100\\cellx200 a\\cell b\\cell\\row\\pard\\pagebb x\\page y\\par}";
        String marked = RtfWordReader.markTablesAndPageBreaks(rtf);

        // the stylesheet and the cell definitions are kept
        assertTrue(marked.startsWith("{\\rtf1{\\stylesheet{\\s1\\pagebb Heading;}}"));
        assertTrue(marked.contains("\\cellx100\\cellx200"));
        assertEquals(2, count(marked, "{\\uc1\\u7?}"));
        assertEquals(2, count(marked, "{\\uc1\\u12?}"));
        assertFalse(marked.contains("\\row"));
    }

    private int count(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    private List<DocxElement> read(String rtf) throws IOException {
        return RtfWordReader.read(new ByteArrayInputStream(rtf.getBytes(ISO_8859_1)));
    }
}
