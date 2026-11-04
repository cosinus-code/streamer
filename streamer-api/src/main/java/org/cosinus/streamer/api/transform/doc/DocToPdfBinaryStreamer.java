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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.EmptyFileException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfTextFont;
import org.cosinus.streamer.api.transform.pdf.PdfTextWriter;
import org.cosinus.streamer.api.transform.pdf.PdfTextWriter.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.clamp;
import static org.apache.poi.poifs.filesystem.FileMagic.prepareToCheckMagic;
import static org.cosinus.streamer.api.transform.pdf.PdfTextWriter.FONT_SIZE;
import static org.cosinus.streamer.api.transform.pdf.PdfTextWriter.INDENT;

/**
 * {@link TransformBinaryStreamer} that converts word content (docx, doc or rtf) into pdf content.
 * <p>
 * The format is detected from the content: office open xml (docx), word 97-2003 (doc) or rich text (rtf).
 * A content which is already pdf, whatever the file extension, is kept as it is.
 * The text of the document is written in order (see {@link PdfTextWriter}), keeping its basic layout:
 * the headings and the font sizes, the paragraph alignment, the bullets and numbers of the lists,
 * the page breaks, and the tables (as simple grids).
 * The rest of the formatting (fonts, styles, colors), the images, the headers and the footers are not kept.
 */
public class DocToPdfBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    static final float TITLE_FONT_SIZE = 20;

    private static final float[] HEADING_FONT_SIZES = {16, 14, 12.5f};

    private static final float MIN_FONT_SIZE = 5;

    private static final float MAX_FONT_SIZE = 36;

    private static final String BULLET = "•";

    public DocToPdfBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    /**
     * A content which is already pdf (e.g. a pdf file saved with a doc extension) is returned as it is.
     */
    @Override
    protected InputStream transform(final InputStream input) throws IOException {
        InputStream content = prepareToCheckMagic(input);
        if (isPdf(content)) {
            return new ByteArrayInputStream(content.readAllBytes());
        }
        return super.transform(content);
    }

    /**
     * @return true if the content is pdf, false otherwise (including an empty content)
     */
    private boolean isPdf(final InputStream content) throws IOException {
        try {
            return FileMagic.valueOf(content) == FileMagic.PDF;
        } catch (EmptyFileException e) {
            return false;
        }
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        InputStream content = prepareToCheckMagic(input);
        try {
            FileMagic format = FileMagic.valueOf(content);
            return switch (format) {
                case OOXML -> DocxWordReader.read(content);
                case OLE2 -> DocWordReader.read(content);
                case RTF -> RtfWordReader.read(content);
                default -> throw new IOException("Unsupported word document format: " + format);
            };
        } catch (RuntimeException e) {
            throw new IOException("Unreadable word document", e);
        }
    }

    @Override
    protected void write(final List<DocxElement> elements, final OutputStream output) throws IOException {
        try (PDDocument document = new PDDocument()) {
            try (PdfTextWriter writer = new PdfTextWriter(document, PdfTextFont.load(document))) {
                ListNumbering numbering = new ListNumbering();
                for (DocxElement element : elements) {
                    switch (element) {
                        case DocxParagraph paragraph -> writeParagraph(paragraph, writer, numbering);
                        case DocxTable table -> writer.writeTable(table.rows());
                    }
                }
            }
            document.save(output);
        }
    }

    private void writeParagraph(final DocxParagraph paragraph,
                                final PdfTextWriter writer,
                                final ListNumbering numbering) throws IOException {
        if (paragraph.pageBreakBefore()) {
            writer.pageBreak();
        }
        String prefix = paragraph.listId() != null ?
            numbering.next(paragraph.listId(), paragraph.listLevel(), paragraph.numberFormat()) :
            null;
        float indent = prefix != null ? paragraph.listLevel() * INDENT : 0;
        writer.writeParagraph(paragraph.text(), fontSize(paragraph), indent, alignment(paragraph), prefix);
    }

    private Alignment alignment(final DocxParagraph paragraph) {
        return switch (paragraph.alignment()) {
            case CENTER -> Alignment.CENTER;
            case RIGHT -> Alignment.RIGHT;
            case LEFT -> Alignment.LEFT;
        };
    }

    /**
     * The font size is given by the heading style of the paragraph, or by the largest size set in its text.
     */
    static float fontSize(final DocxParagraph paragraph) {
        String style = paragraph.style();
        if ("title".equals(style)) {
            return TITLE_FONT_SIZE;
        }
        if (style.matches("heading[1-9]")) {
            int level = style.charAt(style.length() - 1) - '1';
            return level < HEADING_FONT_SIZES.length ? HEADING_FONT_SIZES[level] : FONT_SIZE;
        }
        return paragraph.fontSize() != null ?
            clamp(paragraph.fontSize(), MIN_FONT_SIZE, MAX_FONT_SIZE) :
            FONT_SIZE;
    }

    /**
     * Numbers the items of the lists, per list and level.
     * The numbering of a level restarts after an item of a lower level.
     */
    static class ListNumbering {

        private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

        private static final String[] ROMAN_DIGITS =
            {"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};

        private final Map<String, Map<Integer, Integer>> counters = new HashMap<>();

        /**
         * @param listId the id of the list
         * @param level  the level of the item in the list, starting from 0
         * @param format the number format of the level (e.g. bullet, decimal, lowerLetter), or null
         * @return the prefix of the next item
         */
        String next(final String listId, final int level, final String format) {
            Map<Integer, Integer> levelCounters = counters.computeIfAbsent(listId, id -> new HashMap<>());
            levelCounters.keySet().removeIf(counterLevel -> counterLevel > level);
            int number = levelCounters.merge(level, 1, Integer::sum);
            if (format == null || "bullet".equals(format)) {
                return BULLET;
            }
            return switch (format) {
                case "none" -> "";
                case "lowerLetter" -> letters(number) + ".";
                case "upperLetter" -> letters(number).toUpperCase() + ".";
                case "lowerRoman" -> roman(number) + ".";
                case "upperRoman" -> roman(number).toUpperCase() + ".";
                default -> number + ".";
            };
        }

        private String letters(final int number) {
            StringBuilder letters = new StringBuilder();
            for (int value = number; value > 0; value = (value - 1) / 26) {
                letters.insert(0, (char) ('a' + (value - 1) % 26));
            }
            return letters.toString();
        }

        private String roman(final int number) {
            StringBuilder roman = new StringBuilder();
            int value = number;
            for (int index = 0; index < ROMAN_VALUES.length; index++) {
                for (; value >= ROMAN_VALUES[index]; value -= ROMAN_VALUES[index]) {
                    roman.append(ROMAN_DIGITS[index]);
                }
            }
            return roman.toString();
        }
    }
}
