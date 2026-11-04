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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamer.ListNumbering;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxWordWriter;
import org.cosinus.streamer.api.transform.doc.RtfWordWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer.DOC_FORMAT;

/**
 * {@link TransformBinaryStreamer} that converts html content into a word document.
 * <p>
 * The html is read as paragraphs and tables (see {@link HtmlWordReader}),
 * keeping the headings, the font sizes, the alignment, the line and page breaks, and the tables.
 * As the word writers don't write the numbering of the lists,
 * the list items are written with their number or bullet at the start of their text,
 * indented by their level.
 * <p>
 * The docx format is written by {@link DocxWordWriter}.
 * The doc format is written as rtf (see {@link RtfWordWriter}), which word opens as a word document,
 * as the binary format of word 97-2003 cannot be written.
 */
public class HtmlToDocBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    static final String LIST_INDENT = "    ";

    private final String targetFormat;

    public HtmlToDocBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(binaryStreamer);
        this.targetFormat = targetFormat;
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        return numberLists(HtmlWordReader.read(input));
    }

    @Override
    protected void write(final List<DocxElement> elements, final OutputStream output) throws IOException {
        if (DOC_FORMAT.equalsIgnoreCase(targetFormat)) {
            new RtfWordWriter(elements).write(output);
        } else {
            new DocxWordWriter(elements).write(output);
        }
    }

    /**
     * @return the elements, with the number or bullet of each list item at the start of its text
     */
    static List<DocxElement> numberLists(final List<DocxElement> elements) {
        ListNumbering numbering = new ListNumbering();
        List<DocxElement> numbered = new ArrayList<>(elements.size());
        for (DocxElement element : elements) {
            if (element instanceof DocxParagraph paragraph && paragraph.listId() != null) {
                String number = numbering.next(paragraph.listId(), paragraph.listLevel(), paragraph.numberFormat());
                String prefix = LIST_INDENT.repeat(paragraph.listLevel()) + (number.isEmpty() ? "" : number + " ");
                numbered.add(new DocxParagraph(prefix + paragraph.text(), paragraph.style(), paragraph.fontSize(),
                    paragraph.alignment(), null, 0, null, paragraph.pageBreakBefore()));
            } else {
                numbered.add(element);
            }
        }
        return numbered;
    }
}
