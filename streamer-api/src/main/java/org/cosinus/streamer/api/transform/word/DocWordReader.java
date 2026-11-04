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

package org.cosinus.streamer.api.transform.word;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.HWPFList;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.cosinus.streamer.api.transform.word.WordElement.Alignment;
import org.cosinus.streamer.api.transform.word.WordElement.WordParagraph;
import org.cosinus.streamer.api.transform.word.WordElement.WordTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.api.transform.pdf.WordToPdfBinaryStreamerTransformer.DOC_FORMAT;
import static org.cosinus.streamer.api.transform.word.WordElement.Alignment.CENTER;
import static org.cosinus.streamer.api.transform.word.WordElement.Alignment.LEFT;
import static org.cosinus.streamer.api.transform.word.WordElement.Alignment.RIGHT;

/**
 * Reads the body of a word document in the binary format of word 97-2003 (doc).
 */
public final class DocWordReader {

    private static final char PAGE_BREAK = '\f';

    /**
     * Line breaks: paragraph end, line break (vertical tab), cell and row end.
     */
    private static final Pattern LINE_BREAKS = Pattern.compile("[\\r\\u000B\\u0007]");

    /**
     * The other control characters, marking pictures, objects, footnotes, etc.
     */
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\x00-\\x08\\x0E-\\x1F]");

    /**
     * @param input the doc content
     * @return the elements of the document body
     * @throws IOException if the content cannot be read
     */
    public static List<WordElement> read(final InputStream input) throws IOException {
        try (HWPFDocument document = new HWPFDocument(input)) {
            Range body = document.getRange();
            List<WordElement> elements = new ArrayList<>();
            boolean pageBreak = false;
            int count = body.numParagraphs();
            for (int index = 0; index < count; index++) {
                Paragraph paragraph = body.getParagraph(index);
                if (paragraph.isInTable()) {
                    Table table = body.getTable(paragraph);
                    elements.add(readTable(table));
                    while (index + 1 < count && body.getParagraph(index + 1).getEndOffset() <= table.getEndOffset()) {
                        index++;
                    }
                    continue;
                }

                // a manual page break is a character of the paragraph, splitting it in parts
                String[] parts = clean(paragraph.text()).split(String.valueOf(PAGE_BREAK), -1);
                for (int part = 0; part < parts.length; part++) {
                    pageBreak |= part > 0;
                    if (!parts[part].isEmpty() || parts.length == 1) {
                        elements.add(readParagraph(document, paragraph, parts[part],
                            pageBreak || part == 0 && paragraph.pageBreakBefore()));
                        pageBreak = false;
                    }
                }
            }
            return elements;
        }
    }

    private static WordParagraph readParagraph(final HWPFDocument document,
                                               final Paragraph paragraph,
                                               final String text,
                                               final boolean pageBreakBefore) {
        boolean inList = paragraph.isInList();
        int level = inList ? paragraph.getIlvl() : 0;
        return new WordParagraph(
            text,
            styleName(document, paragraph),
            fontSize(paragraph),
            alignment(paragraph),
            inList ? DOC_FORMAT + ":" + paragraph.getIlfo() : null,
            level,
            inList ? numberFormat(paragraph, level) : null,
            pageBreakBefore);
    }

    /**
     * Remove the field codes (keeping their results) and the control characters, except the line and page breaks.
     */
    static String clean(final String text) {
        String lines = LINE_BREAKS.matcher(Range.stripFields(text)).replaceAll("\n");
        String cleaned = CONTROL_CHARACTERS.matcher(lines).replaceAll("");
        int end = cleaned.length();
        while (end > 0 && cleaned.charAt(end - 1) == '\n') {
            end--;
        }
        return cleaned.substring(0, end);
    }

    private static String styleName(final HWPFDocument document, final Paragraph paragraph) {
        return ofNullable(document.getStyleSheet())
            .map(styles -> styles.getStyleDescription(paragraph.getStyleIndex()))
            .map(StyleDescription::getName)
            .map(name -> name.toLowerCase().replace(" ", ""))
            .orElse("");
    }

    /**
     * The font sizes of word 97-2003 are in half points.
     */
    private static Float fontSize(final Paragraph paragraph) {
        return range(0, paragraph.numCharacterRuns())
            .mapToObj(paragraph::getCharacterRun)
            .map(CharacterRun::getFontSizeAsDouble)
            .filter(halfPoints -> halfPoints != null && halfPoints > 0)
            .max(Double::compare)
            .map(halfPoints -> (float) (halfPoints / 2))
            .orElse(null);
    }

    private static Alignment alignment(final Paragraph paragraph) {
        return switch (paragraph.getJustification()) {
            case 1 -> CENTER;
            case 2 -> RIGHT;
            default -> LEFT;
        };
    }

    /**
     * Map the number format code of word 97-2003 (nfc) to the name used in docx.
     */
    private static String numberFormat(final Paragraph paragraph, final int level) {
        HWPFList list;
        try {
            list = paragraph.getList();
        } catch (RuntimeException e) {
            // the list is not defined
            return null;
        }
        if (list == null) {
            return null;
        }
        return switch (list.getNumberFormat((char) level)) {
            case 0 -> "decimal";
            case 1 -> "upperRoman";
            case 2 -> "lowerRoman";
            case 3 -> "upperLetter";
            case 4 -> "lowerLetter";
            case 23 -> "bullet";
            case 255 -> "none";
            default -> "decimal";
        };
    }

    private static WordTable readTable(final Table table) {
        List<List<String>> rows = new ArrayList<>(table.numRows());
        for (int rowIndex = 0; rowIndex < table.numRows(); rowIndex++) {
            TableRow row = table.getRow(rowIndex);
            List<String> cells = new ArrayList<>(row.numCells());
            for (int cellIndex = 0; cellIndex < row.numCells(); cellIndex++) {
                cells.add(clean(row.getCell(cellIndex).text()).replace(String.valueOf(PAGE_BREAK), ""));
            }
            rows.add(cells);
        }
        return new WordTable(rows);
    }
}
