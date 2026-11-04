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

package org.cosinus.streamer.api.transform.txt;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.pdf.PdfPlainTextWriter;
import org.cosinus.streamer.api.transform.pdf.PdfTextFont;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * {@link TransformBinaryStreamer} that converts plain text into pdf content, as it is printed.
 * <p>
 * The text is decoded and split in lines as for word (see {@link TxtToDocBinaryStreamer}):
 * the encoding is given by the byte order mark, or is utf-8 if valid, or windows-1252,
 * and a form feed starts a new page.
 * The lines are written with a monospaced font, keeping their spaces and tabs (see {@link PdfPlainTextWriter}).
 */
public class TxtToPdfBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    public TxtToPdfBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        return TxtToDocBinaryStreamer.paragraphs(TxtToDocBinaryStreamer.decode(input.readAllBytes()));
    }

    @Override
    protected void write(final List<DocxElement> lines, final OutputStream output) throws IOException {
        try (PDDocument document = new PDDocument()) {
            try (PdfPlainTextWriter writer = new PdfPlainTextWriter(document, PdfTextFont.loadMonospace(document))) {
                for (DocxElement element : lines) {
                    DocxParagraph line = (DocxParagraph) element;
                    if (line.pageBreakBefore()) {
                        writer.pageBreak();
                    }
                    writer.writeLine(line.text());
                }
            }
            document.save(output);
        }
    }
}
