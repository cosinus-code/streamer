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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.text.Normalizer.Form.NFD;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.COURIER;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;

/**
 * Font used to write text in a pdf document.
 * <p>
 * A unicode TrueType font of the system is embedded when available, so that diacritics (e.g. ș, ț) are rendered.
 * Otherwise, the standard Helvetica (or Courier, for a monospaced font) is used,
 * and the characters it cannot encode are replaced by their base letter (e.g. ș becomes s), or by '?'.
 */
public class PdfTextFont {

    private static final List<String> UNICODE_FONT_FILES = List.of(
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/TTF/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
        "/usr/share/fonts/liberation/LiberationSans-Regular.ttf",
        "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf",
        "C:/Windows/Fonts/arial.ttf",
        "/System/Library/Fonts/Supplemental/Arial.ttf",
        "/Library/Fonts/Arial Unicode.ttf");

    private static final List<String> MONOSPACE_FONT_FILES = List.of(
        "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf",
        "/usr/share/fonts/dejavu/DejaVuSansMono.ttf",
        "/usr/share/fonts/TTF/DejaVuSansMono.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationMono-Regular.ttf",
        "/usr/share/fonts/liberation/LiberationMono-Regular.ttf",
        "/usr/share/fonts/truetype/noto/NotoSansMono-Regular.ttf",
        "C:/Windows/Fonts/cour.ttf",
        "/System/Library/Fonts/Supplemental/Courier New.ttf",
        "/System/Library/Fonts/Menlo.ttc");

    private static final char REPLACEMENT = '?';

    private final PDFont font;

    private final Map<Integer, Boolean> supportedCodePoints = new HashMap<>();

    private PdfTextFont(final PDFont font) {
        this.font = font;
    }

    /**
     * Load the font to use in a document.
     *
     * @param document the document to embed the font into
     * @return the font
     */
    public static PdfTextFont load(final PDDocument document) {
        return load(document, UNICODE_FONT_FILES, helvetica());
    }

    /**
     * Load the monospaced font to use in a document, in which all the characters have the same width.
     *
     * @param document the document to embed the font into
     * @return the font
     */
    public static PdfTextFont loadMonospace(final PDDocument document) {
        return load(document, MONOSPACE_FONT_FILES, courier());
    }

    private static PdfTextFont load(final PDDocument document,
                                    final List<String> fontFiles,
                                    final PdfTextFont fallback) {
        for (String fontFile : fontFiles) {
            File file = new File(fontFile);
            if (file.isFile()) {
                try {
                    return new PdfTextFont(PDType0Font.load(document, file));
                } catch (IOException | RuntimeException e) {
                    // not a usable font, try the next one
                }
            }
        }
        return fallback;
    }

    /**
     * @return the fallback font, without diacritics support
     */
    static PdfTextFont helvetica() {
        return new PdfTextFont(new PDType1Font(HELVETICA));
    }

    /**
     * @return the fallback monospaced font, without diacritics support
     */
    static PdfTextFont courier() {
        return new PdfTextFont(new PDType1Font(COURIER));
    }

    public PDFont getFont() {
        return font;
    }

    /**
     * Make a text writable with this font:
     * the line breaks and tabs are replaced by spaces,
     * and the characters the font cannot encode are replaced by their base letter, or by '?'.
     *
     * @param text the text
     * @return the writable text
     */
    public String sanitize(final String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sanitized = new StringBuilder(text.length());
        text.replaceAll("[\\r\\n\\t]+", " ")
            .codePoints()
            .forEach(codePoint -> sanitized.append(writableText(codePoint)));
        return sanitized.toString();
    }

    /**
     * @param text the text, already sanitized
     * @param size the font size
     * @return the width of the text, in points
     * @throws IOException if the width cannot be computed
     */
    public float width(final String text, final float size) throws IOException {
        return font.getStringWidth(text) / 1000 * size;
    }

    private String writableText(final int codePoint) {
        if (isSupported(codePoint)) {
            return Character.toString(codePoint);
        }
        String baseLetters = Normalizer.normalize(Character.toString(codePoint), NFD)
            .replaceAll("\\p{M}", "");
        return !baseLetters.isEmpty() && baseLetters.codePoints().allMatch(this::isSupported) ?
            baseLetters :
            String.valueOf(REPLACEMENT);
    }

    private boolean isSupported(final int codePoint) {
        return supportedCodePoints.computeIfAbsent(codePoint, this::canEncode);
    }

    /**
     * For an embedded TrueType font, the character must have a glyph in the font,
     * as the missing ones are silently encoded as an empty glyph.
     */
    private boolean canEncode(final int codePoint) {
        if (font instanceof PDType0Font type0Font) {
            return type0Font.getCmapLookup() != null &&
                type0Font.getCmapLookup().getGlyphId(codePoint) > 0;
        }
        try {
            return font.encode(Character.toString(codePoint)).length > 0;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }
}
