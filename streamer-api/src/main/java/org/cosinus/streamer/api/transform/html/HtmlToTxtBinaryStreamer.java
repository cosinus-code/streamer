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
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.TxtWordWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * {@link TransformBinaryStreamer} that converts html content into plain text.
 * <p>
 * The html is read as paragraphs and tables (see {@link HtmlWordReader}),
 * and written as text as the word documents are (see {@link TxtWordWriter}):
 * each paragraph on a line, the list items numbered and indented, the table rows with tab separated cells,
 * and the page breaks as form feeds. The text is written in utf-8.
 */
public class HtmlToTxtBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    public HtmlToTxtBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        return HtmlWordReader.read(input);
    }

    @Override
    protected void write(final List<DocxElement> elements, final OutputStream output) throws IOException {
        new TxtWordWriter(elements).write(output);
    }
}
