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

package org.cosinus.streamer.api.transform.html;

import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HtmlWordWriterTest {

    @Test
    public void testParagraphs() {
        assertEquals("""
                <h1>Title</h1>
                <h2>Heading</h2>
                <h6>Deep heading</h6>
                <p style="text-align:center;font-size:12pt">Ștefan &amp; &lt;Iași&gt;</p>
                <p style="text-align:right;font-size:10.5pt">first line<br>second line</p>
                <p></p>
                <p style="break-before:page">next page</p>
                """,
            body(
                paragraph("Title", "title", null, Alignment.LEFT, false),
                paragraph("Heading", "heading2", 16f, Alignment.LEFT, false),
                paragraph("Deep heading", "heading8", null, Alignment.LEFT, false),
                paragraph("Ștefan & <Iași>", "", 12f, Alignment.CENTER, false),
                paragraph("first line\nsecond line", "", 10.5f, Alignment.RIGHT, false),
                paragraph("", "", null, Alignment.LEFT, false),
                paragraph("next page", "", null, Alignment.LEFT, true)));
    }

    /**
     * The levels are nested lists, inside the item before them,
     * and a numbered list continued after a paragraph starts from its next number.
     */
    @Test
    public void testLists() {
        assertEquals("""
                <ol>
                <li>first<ol type="a"><li>nested a</li>
                <li>nested b</li></ol></li>
                <li>second<ul><li>bullet</li></ul></li></ol>
                <p>between</p>
                <ol start="3">
                <li>third</li></ol>
                <ul style="list-style:none">
                <li>no number</li></ul>
                <ol type="I">
                <li>other list</li></ol>
                """,
            body(
                item("first", "one", 0, "decimal"),
                item("nested a", "one", 1, "lowerLetter"),
                item("nested b", "one", 1, "lowerLetter"),
                item("second", "one", 0, "decimal"),
                item("bullet", "one", 1, "bullet"),
                plain("between"),
                item("third", "one", 0, "decimal"),
                item("no number", "two", 0, "none"),
                item("other list", "three", 0, "upperRoman")));
    }

    @Test
    public void testNumberingRestartsAfterALowerLevel() {
        String body = body(
            item("a", "list", 0, "decimal"),
            item("a.1", "list", 1, "decimal"),
            item("a.2", "list", 1, "decimal"),
            item("b", "list", 0, "decimal"),
            item("b.1", "list", 1, "decimal"));
        assertTrue(body.contains("<li>b<ol><li>b.1</li></ol>"));
    }

    @Test
    public void testTable() {
        assertEquals("""
                <p>before</p>
                <table>
                <tr><td>name</td><td>qty</td></tr>
                <tr><td>multi<br>line</td><td></td></tr>
                </table>
                """,
            body(plain("before"), new DocxTable(List.of(List.of("name", "qty"), List.of("multi\nline")))));
    }

    /**
     * The page written is read back with the same paragraphs, lists, tables and page breaks.
     */
    @Test
    public void testRoundTrip() throws IOException {
        List<DocxElement> elements = List.of(
            paragraph("Heading", "heading1", null, Alignment.LEFT, false),
            paragraph("Centered", "", null, Alignment.CENTER, false),
            item("first", "one", 0, "decimal"),
            item("nested", "one", 1, "bullet"),
            item("second", "one", 0, "decimal"),
            new DocxTable(List.of(List.of("name", "qty"), List.of("mere", "12"))),
            paragraph("next page", "", null, Alignment.LEFT, true));

        String html = new HtmlWordWriter(elements, "page").html();
        List<DocxElement> read = HtmlWordReader.read(new ByteArrayInputStream(html.getBytes(UTF_8)));

        assertEquals(elements.size(), read.size());
        DocxParagraph heading = (DocxParagraph) read.get(0);
        assertEquals("Heading", heading.text());
        assertEquals("heading1", heading.style());
        assertEquals(Alignment.CENTER, ((DocxParagraph) read.get(1)).alignment());
        for (int index : new int[]{2, 3, 4}) {
            DocxParagraph expected = (DocxParagraph) elements.get(index);
            DocxParagraph item = (DocxParagraph) read.get(index);
            assertEquals(expected.text(), item.text());
            assertEquals(expected.listLevel(), item.listLevel());
            assertEquals(expected.numberFormat(), item.numberFormat());
        }
        assertEquals(elements.get(5), read.get(5));
        assertEquals("next page", ((DocxParagraph) read.get(6)).text());
        assertTrue(((DocxParagraph) read.get(6)).pageBreakBefore());
    }

    @Test
    public void testTag() {
        assertEquals("h1", HtmlWordWriter.tag("title"));
        assertEquals("h3", HtmlWordWriter.tag("heading3"));
        assertEquals("h6", HtmlWordWriter.tag("heading9"));
        assertEquals("p", HtmlWordWriter.tag(""));
        assertEquals("p", HtmlWordWriter.tag("normal"));
    }

    private String body(DocxElement... elements) {
        return new HtmlWordWriter(List.of(elements), "").body();
    }

    private DocxParagraph paragraph(String text, String style, Float fontSize, Alignment alignment, boolean pageBreak) {
        return new DocxParagraph(text, style, fontSize, alignment, null, 0, null, pageBreak);
    }

    private DocxParagraph plain(String text) {
        return paragraph(text, "", null, Alignment.LEFT, false);
    }

    private DocxParagraph item(String text, String listId, int level, String format) {
        return new DocxParagraph(text, "", null, Alignment.LEFT, listId, level, format, false);
    }
}
