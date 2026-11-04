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

import java.util.List;

/**
 * An element of the body of a word document (docx or doc), with its basic layout.
 */
public sealed interface DocxElement {

    /**
     * The alignment of a paragraph.
     */
    enum Alignment {
        LEFT, CENTER, RIGHT
    }

    /**
     * A paragraph.
     *
     * @param text            the text, with line breaks as '\n'
     * @param style           the name of the paragraph style, lower case without spaces (e.g. "heading1", "title"),
     *                        or an empty string
     * @param fontSize        the largest font size set in the text, or null
     * @param alignment       the alignment
     * @param listId          the id of the list containing the paragraph, or null if not in a list
     * @param listLevel       the level in the list, starting from 0
     * @param numberFormat    the number format of the list level (e.g. bullet, decimal, lowerLetter), or null
     * @param pageBreakBefore true to start the paragraph on a new page
     */
    record DocxParagraph(String text,
                         String style,
                         Float fontSize,
                         Alignment alignment,
                         String listId,
                         int listLevel,
                         String numberFormat,
                         boolean pageBreakBefore) implements DocxElement {

        /**
         * @param text the text
         * @return a paragraph of plain text
         */
        static DocxParagraph plain(final String text) {
            return new DocxParagraph(text, "", null, Alignment.LEFT, null, 0, null, false);
        }
    }

    /**
     * A table.
     *
     * @param rows the rows, as cell texts with line breaks as '\n'
     */
    record DocxTable(List<List<String>> rows) implements DocxElement {
    }
}
