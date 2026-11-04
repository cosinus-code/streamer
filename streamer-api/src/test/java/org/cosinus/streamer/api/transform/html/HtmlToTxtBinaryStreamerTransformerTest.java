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

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HtmlToTxtBinaryStreamerTransformerTest {

    private final HtmlToTxtBinaryStreamerTransformer transformer = new HtmlToTxtBinaryStreamerTransformer();

    @Test
    public void testCanTransformHtmlToTxt() {
        assertTrue(transformer.canTransform(file("page.html"), "txt"));
        assertTrue(transformer.canTransform(file("page.htm"), "txt"));
        assertTrue(transformer.canTransform(file("PAGE.HTML"), "TXT"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("page.html"), "pdf"));
        assertFalse(transformer.canTransform(file("page.html"), "docx"));
        assertFalse(transformer.canTransform(file("page.xhtml"), "txt"));
        assertFalse(transformer.canTransform(file("notes.txt"), "txt"));
        assertFalse(transformer.canTransform(file("page"), "txt"));
        assertFalse(transformer.canTransform(image("page.html", new byte[0]), "txt"));
    }

    @Test
    public void testTransformHtml() {
        String html = """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8"><title>Not in the text</title>
            <style>p { color: red }</style><script>var hidden = 1;</script></head>
            <body>
            <h1>Colinde &amp; obiceiuri</h1>
            <p>Ștefan cântă
               în Iași.<br>Al doilea rând</p>
            <ol><li>first<ul><li>nested</ul><li>second</ol>
            <table><tr><th>name<th>qty<tr><td>mere<td>12</table>
            <pre>  keep
                spaces</pre>
            <p style="page-break-before: always">next page</p>
            </body></html>
            """;
        assertEquals("""
            Colinde & obiceiuri
            Ștefan cântă în Iași.
            Al doilea rând
            1. first
                • nested
            2. second
            name\tqty
            mere\t12
              keep
                spaces
            \fnext page
            """, transform("page.html", html.getBytes(UTF_8)));
    }

    @Test
    public void testCharsetFromMeta() {
        byte[] html = "<meta charset=windows-1250><p>Ştefan îngheaţă</p>".getBytes(Charset.forName("windows-1250"));
        assertEquals("Ştefan îngheaţă\n", transform("page.htm", html));
    }

    @Test
    public void testEmptyHtmlGivesAnEmptyText() {
        assertEquals("", transform("empty.html", new byte[0]));
        assertEquals("", transform("empty.html", "<html><head><title>t</title></head></html>".getBytes(UTF_8)));
    }

    private String transform(String name, byte[] html) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer(name, html), "txt")), UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
