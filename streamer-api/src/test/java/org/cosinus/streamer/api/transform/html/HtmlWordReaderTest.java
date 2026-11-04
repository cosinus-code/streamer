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
import java.nio.charset.Charset;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HtmlWordReaderTest {

    @Test
    public void testHeadingsAndParagraphs() throws IOException {
        List<DocxElement> elements = read("""
            <html><body><h1>Raport</h1><h2>Capitol</h2>
            <p>one
               two <b>bold</b> end<br>next line</p><p></p><div>in div</div></body></html>
            """);
        assertEquals(4, elements.size());
        assertParagraph(elements.get(0), "Raport", "heading1");
        assertParagraph(elements.get(1), "Capitol", "heading2");
        assertParagraph(elements.get(2), "one two bold end\nnext line", "");
        assertParagraph(elements.get(3), "in div", "");
    }

    @Test
    public void testHeadScriptsAndStylesAreSkipped() throws IOException {
        List<DocxElement> elements = read("""
            <html><head><title>Title</title><style>p { color: red }</style></head>
            <body><script>document.write("<p>script</p>");</script><p>text</p><noscript>no script</noscript></body></html>
            """);
        assertEquals(1, elements.size());
        assertParagraph(elements.getFirst(), "text", "");
    }

    @Test
    public void testEntitiesAndDiacritics() throws IOException {
        List<DocxElement> elements = read("<p>Ștefan &amp; Țară &#537;i&nbsp;&#x21B;ar&#259;</p>");
        assertParagraph(elements.getFirst(), "Ștefan & Țară și țară", "");
    }

    @Test
    public void testHtml5Elements() throws IOException {
        List<DocxElement> elements = read("<main><section><header>head</header><article>art</article></section></main>");
        assertEquals(List.of("head", "art"), texts(elements));
    }

    @Test
    public void testAlignment() throws IOException {
        List<DocxElement> elements = read("""
            <p align="center">attribute</p><p style="color: red; text-align: right">style</p>
            <center>center</center><div align="right"><p>inherited</p><p align="left">overridden</p></div>
            """);
        assertEquals(List.of(Alignment.CENTER, Alignment.RIGHT, Alignment.CENTER, Alignment.RIGHT, Alignment.LEFT),
            elements.stream().map(element -> ((DocxParagraph) element).alignment()).toList());
    }

    @Test
    public void testLists() throws IOException {
        List<DocxElement> elements = read("""
            <ol type="a"><li>first<ul><li>nested</li></ul>after nested</li><li>second</ol>
            <ul><li>bullet</ul><ol><li>number</ol>
            """);
        DocxParagraph first = (DocxParagraph) elements.get(0);
        DocxParagraph nested = (DocxParagraph) elements.get(1);
        DocxParagraph afterNested = (DocxParagraph) elements.get(2);
        DocxParagraph second = (DocxParagraph) elements.get(3);

        assertEquals("lowerLetter", first.numberFormat());
        assertEquals(0, first.listLevel());
        assertEquals("bullet", nested.numberFormat());
        assertEquals(1, nested.listLevel());
        assertFalse(nested.listId().equals(first.listId()));
        // the text after a nested list is not a new item
        assertNull(afterNested.listId());
        assertEquals(first.listId(), second.listId());

        assertEquals("bullet", ((DocxParagraph) elements.get(4)).numberFormat());
        assertEquals("decimal", ((DocxParagraph) elements.get(5)).numberFormat());
        // each list is numbered on its own
        assertFalse(((DocxParagraph) elements.get(5)).listId().equals(first.listId()));
    }

    @Test
    public void testTables() throws IOException {
        List<DocxElement> elements = read("""
            <p>before</p>
            <table><tr><th>name<th>qty<tr><td>apples<td>12
            <tr><td>multi<br>line</td><td><table><tr><td>nested</td><td>table</td></tr></table></td></table>
            <p>after</p>
            """);
        assertEquals(3, elements.size());
        assertParagraph(elements.get(0), "before", "");
        assertEquals(List.of(
                List.of("name", "qty"),
                List.of("apples", "12"),
                List.of("multi\nline", "nested table")),
            ((DocxTable) elements.get(1)).rows());
        assertParagraph(elements.get(2), "after", "");
    }

    @Test
    public void testPreformattedText() throws IOException {
        List<DocxElement> elements = read("<pre>\nline 1\n    indented</pre><p>  collapsed   text  </p>");
        assertParagraph(elements.get(0), "line 1\n    indented", "");
        assertParagraph(elements.get(1), "collapsed text", "");
    }

    @Test
    public void testPageBreaks() throws IOException {
        List<DocxElement> elements = read("""
            <p>first</p><p style="page-break-before: always">second</p>
            <div style="break-after: page">third</div><p>fourth</p>
            """);
        assertEquals(List.of(false, true, false, true),
            elements.stream().map(element -> ((DocxParagraph) element).pageBreakBefore()).toList());
    }

    @Test
    public void testUnclosedTags() throws IOException {
        List<DocxElement> elements = read("<p>one<p>two<div><p>three</div>four");
        assertEquals(List.of("one", "two", "three", "four"), texts(elements));
    }

    @Test
    public void testCharset() {
        assertEquals(UTF_8, HtmlWordReader.charset("<p>no charset</p>".getBytes(UTF_8)));
        assertEquals(Charset.forName("windows-1250"),
            HtmlWordReader.charset("<meta charset=\"windows-1250\">".getBytes(UTF_8)));
        assertEquals(Charset.forName("ISO-8859-2"),
            HtmlWordReader.charset("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-2\">"
                .getBytes(UTF_8)));
        assertEquals(UTF_8, HtmlWordReader.charset("<meta charset=\"unknown-charset\">".getBytes(UTF_8)));
        assertEquals(UTF_16LE, HtmlWordReader.charset(new byte[]{(byte) 0xFF, (byte) 0xFE, '<', 0}));
    }

    @Test
    public void testDocumentInCodePageAndWithByteOrderMark() throws IOException {
        Charset windows1250 = Charset.forName("windows-1250");
        List<DocxElement> elements = HtmlWordReader.read(new ByteArrayInputStream(
            "<meta charset=windows-1250><p>Ştefan îngheaţă</p>".getBytes(windows1250)));
        assertParagraph(elements.getFirst(), "Ştefan îngheaţă", "");

        byte[] utf8 = "﻿<p>bom</p>".getBytes(UTF_8);
        assertParagraph(HtmlWordReader.read(new ByteArrayInputStream(utf8)).getFirst(), "bom", "");
    }

    @Test
    public void testEmptyDocument() throws IOException {
        assertTrue(read("").isEmpty());
        assertTrue(read("<html><head><title>only title</title></head><body></body></html>").isEmpty());
    }

    private void assertParagraph(DocxElement element, String text, String style) {
        DocxParagraph paragraph = (DocxParagraph) element;
        assertEquals(text, paragraph.text());
        assertEquals(style, paragraph.style());
    }

    private List<String> texts(List<DocxElement> elements) {
        return elements.stream().map(element -> ((DocxParagraph) element).text()).toList();
    }

    private List<DocxElement> read(String html) throws IOException {
        return HtmlWordReader.read(new ByteArrayInputStream(html.getBytes(UTF_8)));
    }
}
