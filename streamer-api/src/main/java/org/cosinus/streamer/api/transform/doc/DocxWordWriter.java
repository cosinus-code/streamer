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

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Writes word elements as a word document in the office open xml format (docx).
 * <p>
 * A paragraph keeps its line breaks, font size, alignment and page break,
 * and is written in bold when it is a title or a heading.
 * The list information is not written: the paragraphs of a list are written as plain paragraphs.
 */
public class DocxWordWriter {

    /**
     * The characters not allowed in xml, which would make the document unreadable.
     */
    private static final Pattern INVALID_XML_CHARACTERS =
        Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\uFFFE\\uFFFF]");

    private static final Pattern HEADING_STYLE = Pattern.compile("title|heading[1-9]");

    private final List<DocxElement> elements;

    public DocxWordWriter(final List<DocxElement> elements) {

        this.elements = elements;
    }

    /**
     * @param output   the output to write the docx content to
     * @throws IOException if the document cannot be written
     */
    public void write(final OutputStream output) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            for (DocxElement element : elements) {
                switch (element) {
                    case DocxParagraph paragraph -> writeParagraph(document.createParagraph(), paragraph);
                    case DocxTable table -> writeTable(document, table);
                }
            }
            document.write(output);
        }
    }

    private void writeParagraph(final XWPFParagraph paragraph, final DocxParagraph source) {
        paragraph.setPageBreak(source.pageBreakBefore());
        paragraph.setAlignment(switch (source.alignment()) {
            case LEFT -> ParagraphAlignment.LEFT;
            case CENTER -> ParagraphAlignment.CENTER;
            case RIGHT -> ParagraphAlignment.RIGHT;
        });
        XWPFRun run = paragraph.createRun();
        if (source.fontSize() != null) {
            run.setFontSize(source.fontSize().doubleValue());
        }
        if (HEADING_STYLE.matcher(source.style()).matches()) {
            run.setBold(true);
        }
        writeLines(run, source.text());
    }

    private void writeTable(final XWPFDocument document, final DocxTable source) {
        List<List<String>> rows = source.rows();
        int columnCount = rows.stream().mapToInt(List::size).max().orElse(0);
        if (columnCount == 0) {
            return;
        }
        XWPFTable table = document.createTable(rows.size(), columnCount);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            for (int column = 0; column < row.size(); column++) {
                XWPFTableCell cell = table.getRow(rowIndex).getCell(column);
                XWPFParagraph paragraph = cell.getParagraphs().isEmpty() ?
                    cell.addParagraph() :
                    cell.getParagraphs().getFirst();
                writeLines(paragraph.createRun(), row.get(column));
            }
        }
    }

    private void writeLines(final XWPFRun run, final String text) {
        String[] lines = INVALID_XML_CHARACTERS.matcher(text == null ? "" : text).replaceAll("").split("\\R", -1);
        for (int index = 0; index < lines.length; index++) {
            if (index > 0) {
                run.addBreak();
            }
            run.setText(lines[index]);
        }
    }
}
