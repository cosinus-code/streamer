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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlPageTest {

    @Test
    public void testTitle() {
        assertEquals("data", HtmlPage.title("data.csv"));
        assertEquals("data.2026", HtmlPage.title("data.2026.csv"));
        assertEquals(".csv", HtmlPage.title(".csv"));
        assertEquals("notes", HtmlPage.title("notes"));
        assertEquals("", HtmlPage.title(null));
    }

    @Test
    public void testEscape() {
        assertEquals("&lt;b&gt; &amp; &quot;Iași&quot; 'x'", HtmlPage.escape("<b> & \"Iași\" 'x'"));
    }

    @Test
    public void testPage() {
        assertEquals("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <title>a &amp; b</title>
            <style>p{}</style>
            </head>
            <body>
            <p>x</p>
            </body>
            </html>
            """, HtmlPage.page("a & b", "p{}", "<p>x</p>\n"));
    }
}
