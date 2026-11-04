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
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.html.HtmlPage.page;

/**
 * Writes word elements as a html page (see {@link HtmlPage}).
 * <p>
 * The title is written as h1, and the headings as h1-h6.
 * A paragraph keeps its line breaks, its spaces and tabs, its alignment, its font size and its page break
 * (as a break-before style, applied when the page is printed).
 * The list items are written in html lists, nested by their level:
 * the bulleted lists as ul, the numbered lists as ol, with the type of their numbers,
 * and with the number to start from when a list continues after other paragraphs.
 * A table is written with borders, with as many cells in each row as in the longest row.
 */
public class HtmlWordWriter {

    static final String STYLE = "body{font-family:sans-serif}"
        + "p,li,td,h1,h2,h3,h4,h5,h6{white-space:pre-wrap;tab-size:8}ol,ul{white-space:normal}"
        + "p{margin:0 0 .4em}p:empty{min-height:1.2em}"
        + "table{border-collapse:collapse;margin:0 0 .4em}"
        + "td{border:1px solid #999;padding:2px 6px;vertical-align:top}";

    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\r|\\n");

    private static final Pattern HEADING_STYLE = Pattern.compile("heading([1-9])");

    private final List<DocxElement> elements;

    private final String title;

    /**
     * @param elements the elements to write
     * @param title    the title of the page
     */
    public HtmlWordWriter(final List<DocxElement> elements, final String title) {
        this.elements = elements;
        this.title = title;
    }

    /**
     * @param output the output to write the page to, in utf-8
     * @throws IOException if the page cannot be written
     */
    public void write(final OutputStream output) throws IOException {
        output.write(html().getBytes(UTF_8));
    }

    String html() {
        return page(title, STYLE, body());
    }

    String body() {
        return new BodyWriter().write(elements);
    }

    /**
     * A list opened in the html, at a level of a word list.
     */
    private static final class OpenList {

        private final String listId;

        private final int level;

        private final String tag;

        private final String type;

        private boolean itemOpen;

        private OpenList(final String listId, final int level, final String tag, final String type) {
            this.listId = listId;
            this.level = level;
            this.tag = tag;
            this.type = type;
        }
    }

    private static final class BodyWriter {

        private final StringBuilder html = new StringBuilder();

        private final Deque<OpenList> lists = new ArrayDeque<>();

        private final Map<String, Map<Integer, Integer>> counters = new HashMap<>();

        private String write(final List<DocxElement> elements) {
            for (DocxElement element : elements) {
                switch (element) {
                    case DocxParagraph paragraph when paragraph.listId() != null -> writeListItem(paragraph);
                    case DocxParagraph paragraph -> {
                        closeLists();
                        writeParagraph(paragraph);
                    }
                    case DocxTable table -> {
                        closeLists();
                        writeTable(table);
                    }
                }
            }
            closeLists();
            return html.toString();
        }

        private void writeParagraph(final DocxParagraph paragraph) {
            String tag = tag(paragraph.style());
            html.append('<').append(tag)
                .append(styleAttribute(paragraph, "p".equals(tag)))
                .append('>')
                .append(text(paragraph.text()))
                .append("</").append(tag).append(">\n");
        }

        private void writeListItem(final DocxParagraph paragraph) {
            int level = paragraph.listLevel();
            String tag = listTag(paragraph.numberFormat());
            String type = listType(paragraph.numberFormat());

            while (!lists.isEmpty() && lists.peek().level > level) {
                closeList();
            }
            OpenList list = lists.peek();
            if (list != null && list.level == level &&
                (!list.listId.equals(paragraph.listId()) || !list.tag.equals(tag) || !list.type.equals(type))) {
                closeList();
                list = lists.peek();
            }

            int number = nextNumber(paragraph.listId(), level);
            if (list == null || list.level < level) {
                list = openList(paragraph.listId(), level, tag, type, number);
            } else if (list.itemOpen) {
                html.append("</li>\n");
            }
            html.append("<li").append(styleAttribute(paragraph, false)).append('>').append(text(paragraph.text()));
            list.itemOpen = true;
        }

        private OpenList openList(final String listId, final int level, final String tag, final String type,
                                  final int number) {
            html.append('<').append(tag);
            if ("none".equals(type)) {
                // not a type of the html lists: a list without bullets
                html.append(" style=\"list-style:none\"");
            } else if (!type.isEmpty()) {
                html.append(" type=\"").append(type).append('"');
            }
            if ("ol".equals(tag) && number > 1) {
                html.append(" start=\"").append(number).append('"');
            }
            html.append(lists.isEmpty() ? ">\n" : ">");
            OpenList list = new OpenList(listId, level, tag, type);
            lists.push(list);
            return list;
        }

        private void closeList() {
            OpenList list = lists.pop();
            if (list.itemOpen) {
                html.append("</li>");
            }
            html.append("</").append(list.tag).append('>');
            if (lists.isEmpty()) {
                html.append('\n');
            }
        }

        private void closeLists() {
            while (!lists.isEmpty()) {
                closeList();
            }
        }

        /**
         * The numbering of a level restarts after an item of a lower level.
         */
        private int nextNumber(final String listId, final int level) {
            Map<Integer, Integer> levelCounters = counters.computeIfAbsent(listId, id -> new HashMap<>());
            levelCounters.keySet().removeIf(counterLevel -> counterLevel > level);
            return levelCounters.merge(level, 1, Integer::sum);
        }

        private void writeTable(final DocxTable table) {
            if (table.rows().isEmpty()) {
                return;
            }
            int columns = table.rows().stream().mapToInt(List::size).max().orElse(0);
            html.append("<table>\n");
            for (List<String> row : table.rows()) {
                html.append("<tr>");
                for (int column = 0; column < columns; column++) {
                    html.append("<td>").append(column < row.size() ? text(row.get(column)) : "").append("</td>");
                }
                html.append("</tr>\n");
            }
            html.append("</table>\n");
        }
    }

    /**
     * @return the html tag of a paragraph style: h1 for the title, h1-h6 for the headings, p otherwise
     */
    static String tag(final String style) {
        if ("title".equals(style)) {
            return "h1";
        }
        var heading = HEADING_STYLE.matcher(style);
        return heading.matches() ? "h" + Math.min(Integer.parseInt(heading.group(1)), 6) : "p";
    }

    private static String listTag(final String numberFormat) {
        return numberFormat == null || "bullet".equals(numberFormat) || "none".equals(numberFormat) ? "ul" : "ol";
    }

    private static String listType(final String numberFormat) {
        return switch (numberFormat == null ? "" : numberFormat) {
            case "none" -> "none";
            case "lowerLetter" -> "a";
            case "upperLetter" -> "A";
            case "lowerRoman" -> "i";
            case "upperRoman" -> "I";
            default -> "";
        };
    }

    private static String styleAttribute(final DocxParagraph paragraph, final boolean withFontSize) {
        List<String> styles = new ArrayList<>();
        switch (paragraph.alignment()) {
            case CENTER -> styles.add("text-align:center");
            case RIGHT -> styles.add("text-align:right");
            case LEFT -> {
            }
        }
        if (withFontSize && paragraph.fontSize() != null) {
            styles.add("font-size:" + points(paragraph.fontSize()) + "pt");
        }
        if (paragraph.pageBreakBefore()) {
            styles.add("break-before:page");
        }
        return styles.isEmpty() ? "" : " style=\"" + String.join(";", styles) + "\"";
    }

    private static String points(final float size) {
        return size == Math.rint(size) ? String.valueOf((int) size) : String.valueOf(size);
    }

    private static String text(final String text) {
        return String.join("<br>", Arrays.stream(LINE_BREAK.split(text, -1)).map(HtmlPage::escape).toList());
    }
}
