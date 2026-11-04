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

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNum;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFSDT;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.cosinus.streamer.api.transform.word.WordElement.Alignment;
import org.cosinus.streamer.api.transform.word.WordElement.WordParagraph;
import org.cosinus.streamer.api.transform.word.WordElement.WordTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLvl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumFmt;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.api.transform.word.WordElement.Alignment.CENTER;
import static org.cosinus.streamer.api.transform.word.WordElement.Alignment.LEFT;
import static org.cosinus.streamer.api.transform.word.WordElement.Alignment.RIGHT;

/**
 * Reads the body of a word document in the office open xml format (docx).
 */
public final class DocxWordReader {

    /**
     * @param input the docx content
     * @return the elements of the document body
     * @throws IOException if the content cannot be read
     */
    public static List<WordElement> read(final InputStream input) throws IOException {
        try (XWPFDocument document = new XWPFDocument(input)) {
            List<WordElement> elements = new ArrayList<>();
            for (IBodyElement element : document.getBodyElements()) {
                switch (element) {
                    case XWPFParagraph paragraph -> elements.add(readParagraph(document, paragraph));
                    case XWPFTable table -> elements.add(readTable(table));
                    case XWPFSDT contentControl -> elements.add(WordParagraph.plain(contentControl.getContent().getText()));
                    default -> {
                        // nothing to write
                    }
                }
            }
            return elements;
        }
    }

    private static WordParagraph readParagraph(final XWPFDocument document, final XWPFParagraph paragraph) {
        BigInteger numId = paragraph.getNumID();
        int level = ofNullable(paragraph.getNumIlvl()).map(BigInteger::intValue).orElse(0);
        return new WordParagraph(
            paragraph.getText(),
            styleName(document, paragraph),
            fontSize(paragraph),
            alignment(paragraph),
            numId != null ? "docx:" + numId : null,
            level,
            numId != null ? numberFormat(document, numId, level) : null,
            paragraph.isPageBreak());
    }

    /**
     * @return the name of the paragraph style (e.g. "heading 1"), lower case without spaces,
     * or its id when the style is not defined, or an empty string for no style
     */
    private static String styleName(final XWPFDocument document, final XWPFParagraph paragraph) {
        String styleId = paragraph.getStyle();
        if (styleId == null) {
            return "";
        }
        String name = ofNullable(document.getStyles())
            .map(styles -> styles.getStyle(styleId))
            .map(XWPFStyle::getName)
            .orElse(styleId);
        return name.toLowerCase().replace(" ", "");
    }

    private static Float fontSize(final XWPFParagraph paragraph) {
        return paragraph.getRuns().stream()
            .map(XWPFRun::getFontSizeAsDouble)
            .filter(Objects::nonNull)
            .max(Double::compare)
            .map(Double::floatValue)
            .orElse(null);
    }

    private static Alignment alignment(final XWPFParagraph paragraph) {
        ParagraphAlignment alignment = paragraph.getAlignment();
        if (alignment == null) {
            return LEFT;
        }
        return switch (alignment) {
            case CENTER -> CENTER;
            case RIGHT, END -> RIGHT;
            default -> LEFT;
        };
    }

    /**
     * Find the number format of a list level, as {@link XWPFParagraph#getNumFmt()} does,
     * but with the level 0 when the paragraph doesn't give its level (as word does).
     *
     * @return the number format (e.g. bullet, decimal), or null if not defined
     */
    private static String numberFormat(final XWPFDocument document, final BigInteger listId, final int level) {
        return ofNullable(document.getNumbering())
            .flatMap(numbering -> ofNullable(numbering.getNum(listId))
                .map(XWPFNum::getCTNum)
                .map(CTNum::getAbstractNumId)
                .map(CTDecimalNumber::getVal)
                .map(numbering::getAbstractNum))
            .map(XWPFAbstractNum::getCTAbstractNum)
            .flatMap(abstractNum -> abstractNum.getLvlList().stream()
                .filter(lvl -> lvl.getIlvl() != null && lvl.getIlvl().intValue() == level)
                .findFirst())
            .map(CTLvl::getNumFmt)
            .map(CTNumFmt::getVal)
            .map(Object::toString)
            .orElse(null);
    }

    private static WordTable readTable(final XWPFTable table) {
        return new WordTable(table.getRows().stream()
            .map(row -> row.getTableCells().stream()
                .map(XWPFTableCell::getText)
                .toList())
            .toList());
    }
}
