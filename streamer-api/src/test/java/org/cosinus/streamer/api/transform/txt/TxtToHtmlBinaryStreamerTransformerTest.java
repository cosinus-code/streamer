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

package org.cosinus.streamer.api.transform.txt;

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.txt.TxtToDocBinaryStreamer.FALLBACK_CHARSET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TxtToHtmlBinaryStreamerTransformerTest {

    private final TxtToHtmlBinaryStreamerTransformer transformer = new TxtToHtmlBinaryStreamerTransformer();

    @Test
    public void testCanTransformTxtToHtml() {
        assertTrue(transformer.canTransform(file("notes.txt"), "html"));
        assertTrue(transformer.canTransform(file("notes.txt"), "htm"));
        assertTrue(transformer.canTransform(file("NOTES.TXT"), "HTML"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("notes.txt"), "pdf"));
        assertFalse(transformer.canTransform(file("notes.txt"), "xhtml"));
        assertFalse(transformer.canTransform(file("notes.txt"), null));
        assertFalse(transformer.canTransform(file("data.csv"), "html"));
        assertFalse(transformer.canTransform(file("notes"), "html"));
        assertFalse(transformer.canTransform(image("notes.txt", new byte[0]), "html"));
    }

    /**
     * The text is kept as it is, with the html special characters escaped and the line breaks as line feeds.
     */
    @Test
    public void testTransformTxt() {
        String html = transform("notes.txt", "\r\nColinde de Crăciun\r\n  Ștefan\tIași\r\n<b> & \"x\"\n".getBytes(UTF_8));
        assertEquals("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <title>notes</title>
            <style>pre{white-space:pre-wrap;overflow-wrap:anywhere;tab-size:8}.page{break-before:page}</style>
            </head>
            <body>
            <pre>

            Colinde de Crăciun
              Ștefan\tIași
            &lt;b&gt; &amp; &quot;x&quot;
            </pre>
            </body>
            </html>
            """, html);
    }

    @Test
    public void testFormFeedStartsANewPage() {
        String html = transform("notes.txt", "page one\n\fpage two\nmore\fpage three\n\f".getBytes(UTF_8));
        assertTrue(html.contains("""
            <pre>
            page one
            </pre>
            <pre class="page">
            page two
            more</pre>
            <pre class="page">
            page three
            </pre>
            </body>"""));
    }

    @Test
    public void testLegacyEncoding() {
        String html = transform("notes.txt", "café à la crème".getBytes(FALLBACK_CHARSET));
        assertTrue(html.contains("<pre>\ncafé à la crème</pre>"));
    }

    @Test
    public void testEmptyTextGivesAnEmptyPage() {
        String html = transform("empty.txt", new byte[0]);
        assertTrue(html.contains("<title>empty</title>"));
        assertFalse(html.contains("<pre"));
    }

    private String transform(String name, byte[] text) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer(name, text), "html")), UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
