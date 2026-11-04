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

package org.cosinus.streamer.api.transform.html;

import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.doc.DocxElement.Alignment.CENTER;
import static org.cosinus.streamer.api.transform.doc.DocxElement.Alignment.LEFT;
import static org.cosinus.streamer.api.transform.doc.DocxElement.Alignment.RIGHT;

/**
 * Reads the body of a html document as word elements: paragraphs and tables.
 * <p>
 * The html is parsed by the parser of the jdk, which accepts the real-world html (e.g. unclosed tags).
 * The structure of the document is kept: the headings (h1-h6), the paragraphs and line breaks,
 * the alignment (align attribute, text-align style or center), the numbered and bulleted lists,
 * the tables (a nested table is written as text in its cell), the preformatted text
 * and the page breaks (page-break-before/after or break-before/after styles).
 * The rest of the styling (css, fonts, colors), the images, the scripts and the forms are not kept.
 * <p>
 * The encoding is detected from the byte order mark or the meta charset, and is utf-8 by default.
 */
public final class HtmlWordReader {

    private static final Pattern META_CHARSET =
        Pattern.compile("(?i)<meta[^>]+charset\\s*=\\s*[\"']?\\s*([\\w.:-]+)");

    /**
     * The meta charset must be at the start of the document.
     */
    private static final int META_CHARSET_LIMIT = 4096;

    private static final Pattern TEXT_ALIGN = Pattern.compile("(?i)text-align\\s*:\\s*(\\w+)");

    private static final Pattern PAGE_BREAK_BEFORE =
        Pattern.compile("(?i)(page-break-before\\s*:\\s*always|break-before\\s*:\\s*page)");

    private static final Pattern PAGE_BREAK_AFTER =
        Pattern.compile("(?i)(page-break-after\\s*:\\s*always|break-after\\s*:\\s*page)");

    private static final Pattern SPACES = Pattern.compile("[ \\t\\u00A0]+");

    /**
     * The elements whose content is not part of the document text.
     */
    private static final Set<String> SKIPPED = Set.of("head", "title", "style", "script", "noscript", "template");

    /**
     * The elements which start and end a paragraph.
     */
    private static final Set<String> BLOCKS = Set.of(
        "html", "body", "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "pre", "center", "address",
        "section", "article", "header", "footer", "nav", "main", "aside", "figure", "figcaption",
        "dl", "dt", "dd", "form", "fieldset", "legend", "ul", "ol", "li", "hr", "caption", "details", "summary");

    private HtmlWordReader() {
    }

    /**
     * @param input the html content
     * @return the elements of the document body
     * @throws IOException if the content cannot be read
     */
    public static List<DocxElement> read(final InputStream input) throws IOException {
        byte[] bytes = input.readAllBytes();
        String html = new String(bytes, charset(bytes));
        if (html.startsWith("\uFEFF")) {
            html = html.substring(1);
        }
        ElementsBuilder builder = new ElementsBuilder();
        new ParserDelegator().parse(new StringReader(html), builder, true);
        return builder.finish();
    }

    /**
     * @return the charset given by the byte order mark or the meta charset, or utf-8
     */
    static Charset charset(final byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return UTF_8;
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return UTF_16BE;
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE) {
            return UTF_16LE;
        }
        Matcher meta = META_CHARSET.matcher(new String(bytes, 0, min(bytes.length, META_CHARSET_LIMIT), ISO_8859_1));
        if (meta.find()) {
            try {
                return Charset.forName(meta.group(1));
            } catch (IllegalArgumentException e) {
                // unknown charset, use the default one
            }
        }
        return UTF_8;
    }

    /**
     * A block element, with the alignment of its paragraphs.
     */
    private record Block(String tag, Alignment alignment, boolean pageBreakAfter) {
    }

    /**
     * A list, with the id and the number format of its items.
     */
    private record ListContext(String id, String numberFormat) {
    }

    /**
     * Builds the elements from the tags and texts given by the parser.
     */
    private static final class ElementsBuilder extends HTMLEditorKit.ParserCallback {

        private final List<DocxElement> elements = new ArrayList<>();

        private final StringBuilder text = new StringBuilder();

        private final Deque<Block> blocks = new ArrayDeque<>();

        private final Deque<ListContext> lists = new ArrayDeque<>();

        private int listCount;

        private int skipped;

        private int preformatted;

        private String style = "";

        private boolean listItem;

        private boolean pageBreak;

        private int tableDepth;

        private List<List<String>> rows;

        private List<String> row;

        private StringBuilder cell;

        @Override
        public void handleStartTag(final HTML.Tag tag, final MutableAttributeSet attributes, final int position) {
            start(tag.toString(), attributes);
        }

        @Override
        public void handleEndTag(final HTML.Tag tag, final int position) {
            end(tag.toString());
        }

        /**
         * The parser gives the empty elements (e.g. br, img) and the elements it doesn't know (e.g. section)
         * as simple tags, the end tags of the latter with an "endtag" attribute.
         */
        @Override
        public void handleSimpleTag(final HTML.Tag tag, final MutableAttributeSet attributes, final int position) {
            String name = tag.toString();
            if (attributes.isDefined(HTML.Attribute.ENDTAG)) {
                end(name);
            } else if ("br".equals(name)) {
                lineBreak();
            } else {
                start(name, attributes);
                if (!BLOCKS.contains(name) || "hr".equals(name)) {
                    end(name);
                }
            }
        }

        @Override
        public void handleText(final char[] data, final int position) {
            if (skipped > 0) {
                return;
            }
            if (tableDepth > 0) {
                currentCell().append(data);
            } else {
                text.append(data);
            }
        }

        private void start(final String tag, final AttributeSet attributes) {
            if (SKIPPED.contains(tag)) {
                skipped++;
                return;
            }
            if (skipped > 0) {
                return;
            }
            if (startTable(tag)) {
                return;
            }
            if (!BLOCKS.contains(tag)) {
                return;
            }

            endParagraph();
            String styleAttribute = attribute(attributes, HTML.Attribute.STYLE);
            if (PAGE_BREAK_BEFORE.matcher(styleAttribute).find()) {
                pageBreak = true;
            }
            switch (tag) {
                case "ul" -> lists.push(new ListContext("html:" + ++listCount, "bullet"));
                case "ol" -> lists.push(new ListContext("html:" + ++listCount,
                    numberFormat(attribute(attributes, HTML.Attribute.TYPE))));
                case "li" -> listItem = !lists.isEmpty();
                case "pre" -> preformatted++;
                default -> {
                    if (tag.matches("h[1-6]")) {
                        style = "heading" + tag.charAt(1);
                    }
                }
            }
            blocks.push(new Block(tag, alignment(tag, attributes, styleAttribute),
                PAGE_BREAK_AFTER.matcher(styleAttribute).find()));
        }

        private void end(final String tag) {
            if (SKIPPED.contains(tag)) {
                skipped = Math.max(0, skipped - 1);
                return;
            }
            if (skipped > 0) {
                return;
            }
            if (endTable(tag)) {
                return;
            }
            if (!BLOCKS.contains(tag) || blocks.stream().noneMatch(block -> block.tag().equals(tag))) {
                return;
            }

            endParagraph();
            // close the block and the ones left open inside it
            Block block;
            do {
                block = blocks.pop();
                switch (block.tag()) {
                    case "ul", "ol" -> lists.poll();
                    case "li" -> listItem = false;
                    case "pre" -> preformatted--;
                    default -> {
                        if (block.tag().matches("h[1-6]")) {
                            style = "";
                        }
                    }
                }
                pageBreak |= block.pageBreakAfter();
            } while (!block.tag().equals(tag));
        }

        private void lineBreak() {
            if (skipped > 0) {
                return;
            }
            if (tableDepth > 0) {
                currentCell().append('\n');
            } else {
                text.append('\n');
            }
        }

        /**
         * Inside a table, the tags only separate the rows and the cells.
         * A nested table is written as text in its cell.
         *
         * @return true if the tag is handled as part of a table
         */
        private boolean startTable(final String tag) {
            if ("table".equals(tag)) {
                if (tableDepth++ == 0) {
                    endParagraph();
                    rows = new ArrayList<>();
                } else {
                    currentCell().append('\n');
                }
                return true;
            }
            if (tableDepth == 0) {
                return false;
            }
            if (tableDepth == 1 && "tr".equals(tag)) {
                endRow();
                row = new ArrayList<>();
            } else if (tableDepth == 1 && ("td".equals(tag) || "th".equals(tag))) {
                endCell();
                cell = new StringBuilder();
                if (row == null) {
                    row = new ArrayList<>();
                }
            } else if (BLOCKS.contains(tag) || "tr".equals(tag)) {
                currentCell().append('\n');
            } else if ("td".equals(tag) || "th".equals(tag)) {
                currentCell().append(' ');
            }
            return true;
        }

        /**
         * @return true if the tag is handled as part of a table
         */
        private boolean endTable(final String tag) {
            if (tableDepth == 0) {
                return false;
            }
            if ("table".equals(tag)) {
                if (--tableDepth == 0) {
                    endRow();
                    if (!rows.isEmpty()) {
                        elements.add(new DocxTable(List.copyOf(rows)));
                    }
                    rows = null;
                }
            } else if (tableDepth == 1 && "tr".equals(tag)) {
                endRow();
            } else if (tableDepth == 1 && ("td".equals(tag) || "th".equals(tag))) {
                endCell();
            }
            return true;
        }

        private StringBuilder currentCell() {
            if (cell == null) {
                cell = new StringBuilder();
                if (row == null) {
                    row = new ArrayList<>();
                }
            }
            return cell;
        }

        private void endCell() {
            if (cell != null) {
                row.add(normalize(cell.toString(), false));
                cell = null;
            }
        }

        private void endRow() {
            endCell();
            if (row != null && !row.isEmpty()) {
                rows.add(List.copyOf(row));
            }
            row = null;
        }

        /**
         * Add the text written so far as a paragraph.
         */
        private void endParagraph() {
            String paragraph = normalize(text.toString(), preformatted > 0);
            text.setLength(0);
            if (paragraph.isEmpty()) {
                return;
            }
            ListContext list = listItem ? lists.peek() : null;
            elements.add(new DocxParagraph(
                paragraph,
                style,
                null,
                blocks.isEmpty() ? LEFT : blocks.peek().alignment(),
                list != null ? list.id() : null,
                list != null ? lists.size() - 1 : 0,
                list != null ? list.numberFormat() : null,
                pageBreak));
            pageBreak = false;
            // the text after a nested list is not a new item
            listItem = false;
        }

        private List<DocxElement> finish() {
            endParagraph();
            while (tableDepth > 0) {
                endTable("table");
            }
            return elements;
        }

        /**
         * @return the alignment set on the element, or the one of the enclosing block
         */
        private Alignment alignment(final String tag, final AttributeSet attributes, final String styleAttribute) {
            Matcher textAlign = TEXT_ALIGN.matcher(styleAttribute);
            String align = textAlign.find() ? textAlign.group(1) : attribute(attributes, HTML.Attribute.ALIGN);
            return switch (align.toLowerCase()) {
                case "center" -> CENTER;
                case "right", "end" -> RIGHT;
                case "left", "start", "justify" -> LEFT;
                default -> "center".equals(tag) ? CENTER : blocks.isEmpty() ? LEFT : blocks.peek().alignment();
            };
        }

        private String numberFormat(final String type) {
            return switch (type) {
                case "a" -> "lowerLetter";
                case "A" -> "upperLetter";
                case "i" -> "lowerRoman";
                case "I" -> "upperRoman";
                default -> "decimal";
            };
        }

        private String attribute(final AttributeSet attributes, final HTML.Attribute name) {
            Object value = attributes.getAttribute(name);
            return value != null ? value.toString() : "";
        }

        /**
         * The spaces are collapsed and the lines trimmed, except in preformatted text.
         */
        private String normalize(final String value, final boolean preformatted) {
            if (preformatted) {
                return value.replace('\u00A0', ' ').stripTrailing().replaceAll("^\\R+", "");
            }
            return value.lines()
                .map(line -> SPACES.matcher(line).replaceAll(" ").strip())
                .reduce((first, second) -> first + "\n" + second)
                .orElse("")
                .strip();
        }
    }
}
