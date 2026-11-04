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
import org.cosinus.streamer.api.transform.doc.DocxWordWriter;
import org.cosinus.streamer.api.transform.doc.DocxElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * {@link TransformBinaryStreamer} that converts pdf content into word content (docx).
 * <p>
 * The text of the pdf is read as paragraphs (see {@link PdfWordReader})
 * and written as an editable word document (see {@link DocxWordWriter}).
 * A pdf without text (e.g. a scanned document) gives an empty word document.
 */
public class PdfToDocBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    public PdfToDocBinaryStreamer(final BinaryStreamer binaryStreamer) {
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
        new DocxWordWriter(elements).write(output);
    }
}
