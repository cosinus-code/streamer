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

import org.apache.poi.EmptyFileException;
import org.apache.poi.poifs.filesystem.FileMagic;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.apache.poi.poifs.filesystem.FileMagic.prepareToCheckMagic;

/**
 * Reads the body of a word document, with the format detected from the content:
 * office open xml (docx, see {@link DocxWordReader}), word 97-2003 (doc, see {@link DocWordReader})
 * or rich text (rtf, see {@link RtfWordReader}), whatever the file extension.
 */
public final class WordReader {

    private WordReader() {
    }

    /**
     * @param input the word content
     * @return the elements of the document body
     * @throws IOException if the content is not a supported word document, or cannot be read
     */
    public static List<DocxElement> read(final InputStream input) throws IOException {
        InputStream content = prepareToCheckMagic(input);
        try {
            FileMagic format = FileMagic.valueOf(content);
            return switch (format) {
                case OOXML -> DocxWordReader.read(content);
                case OLE2 -> DocWordReader.read(content);
                case RTF -> RtfWordReader.read(content);
                default -> throw new IOException("Unsupported word document format: " + format);
            };
        } catch (RuntimeException e) {
            throw new IOException("Unreadable word document", e);
        }
    }

    /**
     * @param content the content, supporting mark and reset (see {@link FileMagic#prepareToCheckMagic(InputStream)})
     * @return true if the content is pdf (e.g. a pdf file saved with a doc extension),
     * false otherwise (including an empty content)
     * @throws IOException if the content cannot be read
     */
    public static boolean isPdf(final InputStream content) throws IOException {
        try {
            return FileMagic.valueOf(content) == FileMagic.PDF;
        } catch (EmptyFileException e) {
            return false;
        }
    }
}
