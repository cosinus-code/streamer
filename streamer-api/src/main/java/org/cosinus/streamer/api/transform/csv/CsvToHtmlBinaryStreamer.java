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
package org.cosinus.streamer.api.transform.csv;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.html.HtmlPage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.html.HtmlPage.page;
import static org.cosinus.streamer.api.transform.html.HtmlPage.title;

/**
 * {@link TransformBinaryStreamer} that converts csv content into a html page, as a table.
 * <p>
 * The csv content is read as by {@link Cvs} (UTF-8, delimiter detected from the first line),
 * and each csv row is written as a table row, with as many cells as the longest row,
 * so that the table stays a regular grid.
 * The line breaks of a value are written as line breaks of its cell.
 * The page is a standalone html5 document in utf-8 (see {@link HtmlPage}),
 * titled with the name of the csv file (without extension), with a simple style for the table borders.
 */
public class CsvToHtmlBinaryStreamer extends TransformBinaryStreamer<Cvs> {

    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\r|\\n");

    private static final String STYLE =
        "table{border-collapse:collapse}td{border:1px solid #999;padding:2px 6px;vertical-align:top}";

    public CsvToHtmlBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected Cvs read(final InputStream input) throws IOException {
        return new Cvs(input.readAllBytes());
    }

    @Override
    protected void write(final Cvs cvs, final OutputStream output) throws IOException {
        output.write(html(cvs, title(getName())).getBytes(UTF_8));
    }

    static String html(final List<List<String>> rows, final String title) {
        StringBuilder body = new StringBuilder();
        if (!rows.isEmpty()) {
            int columns = rows.stream().mapToInt(List::size).max().orElse(0);
            body.append("<table>\n");
            for (List<String> row : rows) {
                body.append("<tr>");
                for (int column = 0; column < columns; column++) {
                    body.append("<td>")
                        .append(column < row.size() ? cell(row.get(column)) : "")
                        .append("</td>");
                }
                body.append("</tr>\n");
            }
            body.append("</table>\n");
        }
        return page(title, STYLE, body.toString());
    }

    private static String cell(final String value) {
        return String.join("<br>", Arrays.stream(LINE_BREAK.split(value, -1)).map(HtmlPage::escape).toList());
    }
}
