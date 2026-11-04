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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfCell;
import org.cosinus.streamer.api.transform.pdf.PdfTableWriter;
import org.cosinus.streamer.api.transform.pdf.PdfTextFont;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

/**
 * {@link TransformBinaryStreamer} that converts csv content into pdf content.
 * <p>
 * The csv content is read as by {@link Cvs} (UTF-8, delimiter detected from the first line),
 * and written as a table (see {@link PdfTableWriter}), under the name of the csv file.
 * As csv values have no type, the values looking like numbers (e.g. 12, -3.5, 1,350.00, 25%) are aligned to the right.
 */
public class CsvToPdfBinaryStreamer extends TransformBinaryStreamer<Cvs> {

    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d[\\d.,]*%?");

    public CsvToPdfBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected Cvs read(final InputStream input) throws IOException {
        return new Cvs(input.readAllBytes());
    }

    @Override
    protected void write(final Cvs cvs, final OutputStream output) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PdfTextFont font = PdfTextFont.load(document);

            List<List<PdfCell>> rows = cvs.stream()
                .map(values -> values.stream()
                    .map(value -> new PdfCell(font.sanitize(value), isNumber(value)))
                    .toList())
                .toList();
            new PdfTableWriter(document, font)
                .writeTable(ofNullable(getName()).orElse(""), rows);

            PdfTableWriter.ensurePage(document);
            document.save(output);
        }
    }

    static boolean isNumber(final String value) {
        return value != null && NUMBER.matcher(value.trim()).matches();
    }
}
