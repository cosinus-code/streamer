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

package org.cosinus.streamer.api.transform.csv;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxTable;
import org.cosinus.streamer.api.transform.doc.DocxWordWriter;
import org.cosinus.streamer.api.transform.doc.RtfWordWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer.DOC_FORMAT;

/**
 * {@link TransformBinaryStreamer} that converts csv content into a word document, as a table.
 * <p>
 * The csv content is read as by {@link Cvs} (UTF-8, delimiter detected from the first line),
 * and each csv row is written as a table row.
 * The docx format is written by {@link DocxWordWriter}.
 * The doc format is written as rtf (see {@link RtfWordWriter}), which word opens as a word document,
 * as the binary format of word 97-2003 cannot be written.
 */
public class CsvToDocBinaryStreamer extends TransformBinaryStreamer<Cvs> {

    private final String targetFormat;

    public CsvToDocBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(binaryStreamer);
        this.targetFormat = targetFormat;
    }

    @Override
    protected Cvs read(final InputStream input) throws IOException {
        return new Cvs(input.readAllBytes());
    }

    @Override
    protected void write(final Cvs cvs, final OutputStream output) throws IOException {
        List<DocxElement> elements = cvs.isEmpty() ? List.of() : List.of(new DocxTable(List.copyOf(cvs)));
        if (DOC_FORMAT.equalsIgnoreCase(targetFormat)) {
            new RtfWordWriter(elements).write(output);
        } else {
            new DocxWordWriter(elements).write(output);
        }
    }
}
