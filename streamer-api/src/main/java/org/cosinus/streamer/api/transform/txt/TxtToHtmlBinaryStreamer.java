/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.cosinus.streamer.api.transform.txt;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.html.HtmlPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.html.HtmlPage.escape;
import static org.cosinus.streamer.api.transform.html.HtmlPage.page;
import static org.cosinus.streamer.api.transform.html.HtmlPage.title;

/**
 * {@link TransformBinaryStreamer} that converts plain text into a html page, showing the text as it is.
 * <p>
 * The text is decoded as for word (see {@link TxtToDocBinaryStreamer}):
 * the encoding is given by the byte order mark, or is utf-8 if valid, or windows-1252.
 * It is written as preformatted text, in a monospaced font, keeping its spaces, tabs and empty lines,
 * with the long lines wrapped to the width of the window.
 * A form feed starts a new page when the html page is printed.
 * <p>
 * The page is a standalone html5 document in utf-8 (see {@link HtmlPage}),
 * titled with the name of the text file (without extension).
 */
public class TxtToHtmlBinaryStreamer extends TransformBinaryStreamer<String> {

    private static final String STYLE =
        "pre{white-space:pre-wrap;overflow-wrap:anywhere;tab-size:8}.page{break-before:page}";

    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\r");

    private static final String PAGE_BREAK = "\f";

    public TxtToHtmlBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected String read(final InputStream input) throws IOException {
        return TxtToDocBinaryStreamer.decode(input.readAllBytes());
    }

    @Override
    protected void write(final String text, final OutputStream output) throws IOException {
        output.write(html(text, title(getName())).getBytes(UTF_8));
    }

    /**
     * @return the html page with the text, a pre block for each page of the text
     */
    static String html(final String text, final String title) {
        StringBuilder body = new StringBuilder();
        boolean firstPage = true;
        for (String pageText : LINE_BREAK.matcher(text).replaceAll("\n").split(PAGE_BREAK, -1)) {
            if (pageText.isEmpty()) {
                continue;
            }
            // the line break right after the start tag is dropped by the html parsers,
            // so it keeps a first empty line of the text
            body.append(firstPage ? "<pre>\n" : "<pre class=\"page\">\n")
                .append(escape(pageText))
                .append("</pre>\n");
            firstPage = false;
        }
        return page(title, STYLE, body.toString());
    }
}
