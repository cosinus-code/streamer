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

import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.cosinus.streamer.api.transform.doc.DocxElement.Alignment.CENTER;
import static org.cosinus.streamer.api.transform.doc.DocxElement.Alignment.LEFT;
import static org.cosinus.streamer.api.transform.doc.DocxElement.Alignment.RIGHT;

/**
 * Reads the body of a document in the rich text format (rtf).
 * <p>
 * The rtf is parsed by the {@link RTFEditorKit} of the jdk, which keeps the text, the paragraph styles,
 * the alignment and the font sizes, but ignores the tables and the page breaks.
 * So these are first marked in the rtf with characters which are kept in the text:
 * the end of a cell with the cell mark of word ({@value #CELL_MARK} as unicode),
 * the end of a row with a paragraph end, and the page breaks with a form feed.
 * A paragraph with cell marks is a table row, and consecutive rows are a table.
 * The numbers and bullets of the lists are kept in the text of their paragraphs.
 * <p>
 * The editor kit also ignores the code page of the document (e.g. {@code \ansicpg1250} for romanian),
 * so the characters escaped in that code page (e.g. {@code \'e3} for ă) are first escaped as unicode.
 * The code pages of the fonts ({@code \fcharset}) are not handled.
 */
public final class RtfWordReader {

    private static final char CELL_MARK = '\u0007';

    private static final char PAGE_BREAK = '\f';

    /**
     * The control words are matched only when not followed by a letter (e.g. not {@code \cellx} or {@code \pagebb}),
     * with the space ending them, which is not part of the text.
     */
    private static final Pattern CELL = Pattern.compile("\\\\cell(?![a-zA-Z]) ?");

    private static final Pattern ROW = Pattern.compile("\\\\row(?![a-zA-Z]) ?");

    private static final Pattern PAGE = Pattern.compile("\\\\page(?![a-zA-Z]) ?");

    private static final Pattern PAGE_BREAK_BEFORE = Pattern.compile("\\\\pagebb(?![a-zA-Z]) ?");

    private static final Pattern CODE_PAGE = Pattern.compile("\\\\ansicpg(\\d+)");

    /**
     * A character escaped in the code page, except the one following a unicode escape,
     * which is only its replacement for the readers without unicode support.
     */
    private static final Pattern CODE_PAGE_CHARACTER =
        Pattern.compile("(?<!\\\\u-?\\d{1,6} ?)\\\\'([0-9a-fA-F]{2})");

    private static final int DEFAULT_CODE_PAGE = 1252;

    private static final Pattern STYLESHEET = Pattern.compile("\\{\\\\stylesheet");

    private static final String CELL_MARK_RTF = "{\\\\uc1\\\\u7?}";

    private static final String PAGE_BREAK_RTF = "{\\\\uc1\\\\u12?}";

    private RtfWordReader() {
    }

    /**
     * @param input the rtf content
     * @return the elements of the document body
     * @throws IOException if the content cannot be read
     */
    public static List<DocxElement> read(final InputStream input) throws IOException {
        DefaultStyledDocument document = new DefaultStyledDocument();
        try {
            byte[] rtf = markTablesAndPageBreaks(new String(input.readAllBytes(), ISO_8859_1)).getBytes(ISO_8859_1);
            new RTFEditorKit().read(new ByteArrayInputStream(rtf), document, 0);
            return readElements(document);
        } catch (BadLocationException e) {
            throw new IOException("Unreadable rtf document", e);
        }
    }

    /**
     * The rtf is 7-bit text (the other characters are escaped), read as iso-8859-1 to keep the bytes as they are.
     * The stylesheet is kept as it is, as the names of the styles are its text.
     */
    static String markTablesAndPageBreaks(final String rtf) {
        int bodyStart = bodyStart(rtf);
        String body = rtf.substring(bodyStart);
        body = CELL.matcher(body).replaceAll(CELL_MARK_RTF);
        body = ROW.matcher(body).replaceAll("\\\\par ");
        body = PAGE.matcher(body).replaceAll(PAGE_BREAK_RTF);
        body = PAGE_BREAK_BEFORE.matcher(body).replaceAll("\\\\pagebb" + PAGE_BREAK_RTF);
        body = escapeCodePageCharacters(body, codePage(rtf));
        return rtf.substring(0, bodyStart) + body;
    }

    /**
     * @return the code page of the document, if it is not the default one and is supported, or null
     */
    private static Charset codePage(final String rtf) {
        Matcher codePage = CODE_PAGE.matcher(rtf);
        if (!codePage.find() || Integer.parseInt(codePage.group(1)) == DEFAULT_CODE_PAGE) {
            return null;
        }
        String name = "windows-" + codePage.group(1);
        return Charset.isSupported(name) ? Charset.forName(name) : null;
    }

    private static String escapeCodePageCharacters(final String body, final Charset codePage) {
        if (codePage == null) {
            return body;
        }
        return CODE_PAGE_CHARACTER.matcher(body).replaceAll(match -> {
            byte character = (byte) Integer.parseInt(match.group(1), 16);
            int codePoint = new String(new byte[]{character}, codePage).codePointAt(0);
            return codePoint < 0x80 ?
                Matcher.quoteReplacement(match.group()) :
                "{\\\\uc1\\\\u" + (short) codePoint + "?}";
        });
    }

    /**
     * @return the index after the stylesheet group, or 0 if there is no stylesheet
     */
    private static int bodyStart(final String rtf) {
        Matcher stylesheet = STYLESHEET.matcher(rtf);
        if (!stylesheet.find()) {
            return 0;
        }
        int depth = 0;
        for (int index = stylesheet.start(); index < rtf.length(); index++) {
            char character = rtf.charAt(index);
            if (character == '\\') {
                index++;
            } else if (character == '{') {
                depth++;
            } else if (character == '}' && --depth == 0) {
                return index + 1;
            }
        }
        return 0;
    }

    private static List<DocxElement> readElements(final DefaultStyledDocument document) throws BadLocationException {
        List<DocxElement> elements = new ArrayList<>();
        List<List<String>> tableRows = new ArrayList<>();
        boolean pageBreak = false;

        Element root = document.getDefaultRootElement();
        for (int index = 0; index < root.getElementCount(); index++) {
            Element paragraph = root.getElement(index);
            String text = document.getText(paragraph.getStartOffset(),
                paragraph.getEndOffset() - paragraph.getStartOffset());
            text = text.endsWith("\n") ? text.substring(0, text.length() - 1) : text;

            if (text.indexOf(CELL_MARK) >= 0) {
                List<String> cells = new ArrayList<>(Arrays.asList(text.split(String.valueOf(CELL_MARK), -1)));
                if (cells.size() > 1 && cells.getLast().isBlank()) {
                    cells.removeLast();
                }
                tableRows.add(cells.stream().map(cell -> cell.replace(String.valueOf(PAGE_BREAK), "").strip()).toList());
                continue;
            }
            if (!tableRows.isEmpty()) {
                elements.add(new DocxTable(List.copyOf(tableRows)));
                tableRows.clear();
            }
            if (text.isEmpty() && index == root.getElementCount() - 1) {
                // the last paragraph of the editor kit, always empty
                continue;
            }

            // a page break is a character of the paragraph, splitting it in parts
            String[] parts = text.split(String.valueOf(PAGE_BREAK), -1);
            for (int part = 0; part < parts.length; part++) {
                pageBreak |= part > 0;
                if (!parts[part].isEmpty() || parts.length == 1) {
                    elements.add(readParagraph(paragraph, parts[part], pageBreak));
                    pageBreak = false;
                }
            }
        }
        if (!tableRows.isEmpty()) {
            elements.add(new DocxTable(List.copyOf(tableRows)));
        }
        return elements;
    }

    private static DocxParagraph readParagraph(final Element paragraph, final String text, final boolean pageBreakBefore) {
        AttributeSet attributes = paragraph.getAttributes();
        return new DocxParagraph(
            text,
            styleName(attributes),
            fontSize(paragraph),
            alignment(attributes),
            null,
            0,
            null,
            pageBreakBefore);
    }

    /**
     * @return the name of the paragraph style (e.g. "Heading 1"), lower case without spaces, or an empty string
     */
    private static String styleName(final AttributeSet attributes) {
        return ofNullable(attributes.getResolveParent())
            .map(style -> style.getAttribute(StyleConstants.NameAttribute))
            .map(name -> name.toString().toLowerCase().replace(" ", ""))
            .orElse("");
    }

    private static Float fontSize(final Element paragraph) {
        return range(0, paragraph.getElementCount())
            .mapToObj(paragraph::getElement)
            .map(Element::getAttributes)
            .filter(attributes -> attributes.isDefined(StyleConstants.FontSize))
            .map(StyleConstants::getFontSize)
            .filter(size -> size > 0)
            .max(Integer::compare)
            .map(Integer::floatValue)
            .orElse(null);
    }

    private static Alignment alignment(final AttributeSet attributes) {
        return switch (StyleConstants.getAlignment(attributes)) {
            case StyleConstants.ALIGN_CENTER -> CENTER;
            case StyleConstants.ALIGN_RIGHT -> RIGHT;
            default -> LEFT;
        };
    }
}
