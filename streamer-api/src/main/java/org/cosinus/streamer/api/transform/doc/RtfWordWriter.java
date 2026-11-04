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

import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * Writes word elements as a document in the rich text format (rtf).
 * <p>
 * It is the way to write a doc file, as the binary format of word 97-2003 cannot be written:
 * word and the other office applications open a rtf content with a doc extension as a word document.
 * <p>
 * The pages are A4 with one inch margins.
 * A paragraph keeps its line breaks, font size, alignment and page break,
 * and is written in bold when it is a title or a heading.
 * A table is written with borders and equal column widths.
 * The list information is not written: the paragraphs of a list are written as plain paragraphs.
 * The text is written as 7-bit ascii, with the other characters escaped as unicode.
 */
public class RtfWordWriter {

    /**
     * A4 page and margins, in twips (1/20 of a point).
     */
    private static final int PAGE_WIDTH = 11906;

    private static final int PAGE_HEIGHT = 16838;

    private static final int MARGIN = 1440;

    static final int TEXT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    /**
     * Space between the text of a cell and its borders, in twips.
     */
    private static final int CELL_GAP = 108;

    private static final Pattern HEADING_STYLE = Pattern.compile("title|heading[1-9]");

    private static final String HEADER = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\uc1"
        + "{\\fonttbl{\\f0\\fswiss\\fcharset0 Arial;}}"
        + "\\paperw" + PAGE_WIDTH + "\\paperh" + PAGE_HEIGHT
        + "\\margl" + MARGIN + "\\margr" + MARGIN + "\\margt" + MARGIN + "\\margb" + MARGIN
        + "\n";

    private static final String CELL_BORDERS =
        "\\clbrdrt\\brdrs\\brdrw10\\clbrdrl\\brdrs\\brdrw10\\clbrdrb\\brdrs\\brdrw10\\clbrdrr\\brdrs\\brdrw10";

    private final List<DocxElement> elements;

    /**
     * @param elements the elements to write
     */
    public RtfWordWriter(final List<DocxElement> elements) {
        this.elements = elements;
    }

    /**
     * @param output the output to write the rtf content to
     * @throws IOException if the document cannot be written
     */
    public void write(final OutputStream output) throws IOException {
        StringBuilder rtf = new StringBuilder(HEADER);
        for (DocxElement element : elements) {
            switch (element) {
                case DocxParagraph paragraph -> writeParagraph(rtf, paragraph);
                case DocxTable table -> writeTable(rtf, table);
            }
        }
        rtf.append("}");
        output.write(rtf.toString().getBytes(US_ASCII));
    }

    private void writeParagraph(final StringBuilder rtf, final DocxParagraph paragraph) {
        if (paragraph.pageBreakBefore()) {
            rtf.append("\\page\n");
        }
        rtf.append("\\pard\\plain").append(switch (paragraph.alignment()) {
            case LEFT -> "\\ql";
            case CENTER -> "\\qc";
            case RIGHT -> "\\qr";
        });
        if (paragraph.fontSize() != null) {
            rtf.append("\\fs").append(Math.round(paragraph.fontSize() * 2));
        }
        if (HEADING_STYLE.matcher(paragraph.style()).matches()) {
            rtf.append("\\b");
        }
        rtf.append(' ').append(text(paragraph.text())).append("\\par\n");
    }

    private void writeTable(final StringBuilder rtf, final DocxTable table) {
        int columnCount = table.rows().stream().mapToInt(List::size).max().orElse(0);
        if (columnCount == 0) {
            return;
        }
        int columnWidth = TEXT_WIDTH / columnCount;

        for (List<String> row : table.rows()) {
            rtf.append("\\trowd\\trgaph").append(CELL_GAP);
            for (int column = 1; column <= columnCount; column++) {
                rtf.append(CELL_BORDERS).append("\\cellx").append(column * columnWidth);
            }
            rtf.append("\n\\pard\\plain\\intbl ");
            for (int column = 0; column < columnCount; column++) {
                rtf.append(column < row.size() ? text(row.get(column)) : "").append("\\cell ");
            }
            rtf.append("\\row\n");
        }
        rtf.append("\\pard\\plain\\par\n");
    }

    /**
     * Escape a text for rtf: the special characters are escaped, the line breaks and tabs written
     * as control words, the other control characters removed and the non-ascii characters escaped as unicode.
     */
    static String text(final String text) {
        StringBuilder escaped = new StringBuilder();
        String value = text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\', '{', '}' -> escaped.append('\\').append(character);
                case '\n' -> escaped.append("\\line ");
                case '\t' -> escaped.append("\\tab ");
                default -> {
                    if (character >= 0x20 && character < 0x7F) {
                        escaped.append(character);
                    } else if (character >= 0x80) {
                        // rtf unicode values are signed 16-bit, the surrogate pairs are escaped one by one
                        escaped.append("\\u").append((short) character).append('?');
                    }
                }
            }
        }
        return escaped.toString();
    }
}
