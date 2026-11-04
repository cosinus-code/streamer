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

package org.cosinus.streamer.api.transform.doc;

import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamer.ListNumbering;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Writes word elements as plain text, as word writes a document saved as plain text:
 * each paragraph on a line, keeping its line breaks,
 * the list items with their numbers or bullets, indented by their level,
 * and each table row on a line, with the cells separated by tabs.
 * A page break is written as a form feed, which starts a new page when the text is printed.
 * The text is written in utf-8, with \n line breaks.
 */
public class TxtWordWriter {

    static final String LIST_INDENT = "    ";

    private static final char PAGE_BREAK = '\f';

    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\r|\\n");

    private static final Pattern CELL_SPACES = Pattern.compile("\\s*(\\r\\n|\\r|\\n|\\t)\\s*");

    private final List<DocxElement> elements;

    /**
     * @param elements the elements to write
     */
    public TxtWordWriter(final List<DocxElement> elements) {
        this.elements = elements;
    }

    /**
     * @param output the output to write the text to, in utf-8
     * @throws IOException if the text cannot be written
     */
    public void write(final OutputStream output) throws IOException {
        output.write(text(elements).getBytes(UTF_8));
    }

    static String text(final List<DocxElement> elements) {
        StringBuilder text = new StringBuilder();
        ListNumbering numbering = new ListNumbering();
        for (DocxElement element : elements) {
            switch (element) {
                case DocxParagraph paragraph -> writeParagraph(text, paragraph, numbering);
                case DocxTable table -> writeTable(text, table);
            }
        }
        return text.toString();
    }

    private static void writeParagraph(final StringBuilder text,
                                       final DocxParagraph paragraph,
                                       final ListNumbering numbering) {
        if (paragraph.pageBreakBefore() && !text.isEmpty()) {
            text.append(PAGE_BREAK);
        }
        String prefix = "";
        if (paragraph.listId() != null) {
            String number = numbering.next(paragraph.listId(), paragraph.listLevel(), paragraph.numberFormat());
            prefix = LIST_INDENT.repeat(paragraph.listLevel()) + (number.isEmpty() ? "" : number + " ");
        }
        // the next lines of a list item are aligned with its text
        String continuation = " ".repeat(prefix.length());
        String[] lines = LINE_BREAK.split(paragraph.text(), -1);
        for (int index = 0; index < lines.length; index++) {
            text.append(index == 0 ? prefix : continuation).append(lines[index]).append('\n');
        }
    }

    /**
     * Each row on a line, with the cells separated by tabs, so the line breaks and tabs of a cell are spaces.
     */
    private static void writeTable(final StringBuilder text, final DocxTable table) {
        for (List<String> row : table.rows()) {
            text.append(String.join("\t", row.stream()
                    .map(cell -> CELL_SPACES.matcher(cell).replaceAll(" ").strip())
                    .toList()))
                .append('\n');
        }
    }
}
