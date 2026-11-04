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

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.cosinus.streamer.api.transform.html.HtmlWordReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsvToHtmlBinaryStreamerTransformerTest {

    private final CsvToHtmlBinaryStreamerTransformer transformer = new CsvToHtmlBinaryStreamerTransformer();

    @Test
    public void testCanTransformCsvToHtml() {
        assertTrue(transformer.canTransform(file("data.csv"), "html"));
        assertTrue(transformer.canTransform(file("data.csv"), "htm"));
        assertTrue(transformer.canTransform(file("DATA.CSV"), "HTML"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("data.csv"), "pdf"));
        assertFalse(transformer.canTransform(file("data.csv"), "xhtml"));
        assertFalse(transformer.canTransform(file("data.csv"), null));
        assertFalse(transformer.canTransform(file("data.txt"), "html"));
        assertFalse(transformer.canTransform(file("data"), "html"));
        assertFalse(transformer.canTransform(image("data.csv", new byte[0]), "html"));
    }

    @Test
    public void testTransformCsv() {
        String html = transform("data.csv", "name;qty\nmere;12\n");
        assertEquals("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <title>data</title>
            <style>table{border-collapse:collapse}td{border:1px solid #999;padding:2px 6px;vertical-align:top}</style>
            </head>
            <body>
            <table>
            <tr><td>name</td><td>qty</td></tr>
            <tr><td>mere</td><td>12</td></tr>
            </table>
            </body>
            </html>
            """, html);
    }

    /**
     * The special characters are escaped, the line breaks of a value are kept,
     * and the short rows are completed with empty cells.
     */
    @Test
    public void testValuesAreEscapedAndRowsCompleted() throws IOException {
        String csv = "a,b,c\n\"<b>Ștefan</b> & \"\"Iași\"\"\",\"line 1\nline 2\"\nshort\n";
        String html = transform("data.csv", csv);
        assertTrue(html.contains("<td>&lt;b&gt;Ștefan&lt;/b&gt; &amp; &quot;Iași&quot;</td><td>line 1<br>line 2</td><td></td>"));
        assertTrue(html.contains("<tr><td>short</td><td></td><td></td></tr>"));

        List<DocxElement> elements = HtmlWordReader.read(new ByteArrayInputStream(html.getBytes(UTF_8)));
        assertEquals(List.of(
                List.of("a", "b", "c"),
                List.of("<b>Ștefan</b> & \"Iași\"", "line 1\nline 2", ""),
                List.of("short", "", "")),
            ((DocxTable) elements.getFirst()).rows());
    }

    @Test
    public void testEmptyCsvGivesAPageWithoutTable() {
        String html = transform("empty.csv", "");
        assertTrue(html.contains("<title>empty</title>"));
        assertFalse(html.contains("<table>"));
    }

    @Test
    public void testTitle() {
        assertEquals("data", CsvToHtmlBinaryStreamer.title("data.csv"));
        assertEquals("data.2026", CsvToHtmlBinaryStreamer.title("data.2026.csv"));
        assertEquals(".csv", CsvToHtmlBinaryStreamer.title(".csv"));
        assertEquals("", CsvToHtmlBinaryStreamer.title(null));
    }

    private String transform(String name, String csv) {
        byte[] html = readAll(transformer.transformStreamer(new TestBinaryStreamer(name, csv.getBytes(UTF_8)), "html"));
        return new String(html, UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
