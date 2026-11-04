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

package org.cosinus.streamer.api.transform.pdf;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.TxtWordWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * {@link TransformBinaryStreamer} that converts pdf content into plain text.
 * <p>
 * The text of the pdf is read as paragraphs, as for word (see {@link PdfWordReader}):
 * the lines of a paragraph are joined, so that each paragraph is a line of the text,
 * and each pdf page after the first one starts with a form feed.
 * The text is written in utf-8 (see {@link TxtWordWriter}).
 * A pdf without text (e.g. a scanned document) gives an empty text.
 */
public class PdfToTxtBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    public PdfToTxtBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        try (PdfWordReader reader = new PdfWordReader(input.readAllBytes())) {
            return reader.read();
        }
    }

    @Override
    protected void write(final List<DocxElement> elements, final OutputStream output) throws IOException {
        new TxtWordWriter(elements).write(output);
    }
}
