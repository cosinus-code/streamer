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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;
import org.cosinus.streamer.api.transform.doc.DocxElement.Alignment;
import org.cosinus.streamer.api.transform.doc.DocxElement.DocxParagraph;
import org.cosinus.streamer.api.transform.doc.DocxWordWriter;
import org.cosinus.streamer.api.transform.doc.RtfWordWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.nio.charset.CodingErrorAction.REPORT;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer.DOC_FORMAT;

/**
 * {@link TransformBinaryStreamer} that converts plain text into a word document.
 * <p>
 * Each line of the text is written as a paragraph, keeping its spaces and tabs,
 * and the empty lines as empty paragraphs, so that the layout of the text is kept.
 * A form feed (used in text files to start a new page when printed) is written as a page break.
 * <p>
 * The encoding is given by the byte order mark (utf-8 or utf-16), or is utf-8 if the text is valid utf-8,
 * or windows-1252 otherwise.
 * <p>
 * The docx format is written by {@link DocxWordWriter}.
 * The doc format is written as rtf (see {@link RtfWordWriter}), which word opens as a word document,
 * as the binary format of word 97-2003 cannot be written.
 */
public class TxtToDocBinaryStreamer extends TransformBinaryStreamer<List<DocxElement>> {

    static final Charset FALLBACK_CHARSET = Charset.forName("windows-1252");

    private static final char PAGE_BREAK = '\f';

    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\r|\\n");

    private final String targetFormat;

    public TxtToDocBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(binaryStreamer);
        this.targetFormat = targetFormat;
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        return paragraphs(decode(input.readAllBytes()));
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
     * @return the text, decoded with the charset of its byte order mark, or as utf-8 if valid, or as windows-1252
     */
    static String decode(final byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, UTF_8);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, 2, bytes.length - 2, UTF_16BE);
        }
        if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE) {
            return new String(bytes, 2, bytes.length - 2, UTF_16LE);
        }
        try {
            return UTF_8.newDecoder()
                .onMalformedInput(REPORT)
                .onUnmappableCharacter(REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, FALLBACK_CHARSET);
        }
    }

    /**
     * @return a paragraph for each line of the text, with a page break before the text following a form feed
     */
    static List<DocxElement> paragraphs(final String text) {
        List<DocxElement> paragraphs = new ArrayList<>();
        if (text.isEmpty()) {
            return paragraphs;
        }
        // not \R, which also matches the form feed
        String[] lines = LINE_BREAK.split(text, -1);
        // the line break ending the last line doesn't start a new one
        int lineCount = lines[lines.length - 1].isEmpty() ? lines.length - 1 : lines.length;

        boolean pageBreak = false;
        for (int index = 0; index < lineCount; index++) {
            String[] parts = lines[index].split(String.valueOf(PAGE_BREAK), -1);
            for (int part = 0; part < parts.length; part++) {
                pageBreak |= part > 0;
                if (!parts[part].isEmpty() || parts.length == 1) {
                    paragraphs.add(new DocxParagraph(parts[part], "", null, Alignment.LEFT, null, 0, null, pageBreak));
                    pageBreak = false;
                }
            }
        }
        return paragraphs;
    }
}
