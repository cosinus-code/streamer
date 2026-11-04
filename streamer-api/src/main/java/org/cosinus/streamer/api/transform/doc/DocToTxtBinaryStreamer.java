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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfWordReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.apache.poi.poifs.filesystem.FileMagic.prepareToCheckMagic;

/**
 * {@link TransformBinaryStreamer} that converts word content (docx, doc or rtf) into plain text.
 * <p>
 * The format is detected from the content (see {@link WordReader}),
 * and the text is written as word writes it when saved as plain text (see {@link TxtWordWriter}).
 * <p>
 * A content which is already pdf (e.g. a pdf file saved with a doc extension) is read as pdf text.
 */
public class DocToTxtBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    public DocToTxtBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        InputStream content = prepareToCheckMagic(input);
        if (WordReader.isPdf(content)) {
            try (PdfWordReader reader = new PdfWordReader(content.readAllBytes())) {
                return reader.read();
            }
        }
        return WordReader.read(content);
    }

    @Override
    protected void write(final List<DocxElement> elements, final OutputStream output) throws IOException {
        new TxtWordWriter(elements).write(output);
    }
}
