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

package org.cosinus.streamer.api.transform.svg;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.svg.SvgRenderer.SvgImage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory.createFromImage;

/**
 * {@link TransformBinaryStreamer} that converts svg content into a single page pdf.
 * <p>
 * The page has the size of the svg, as printed: the svg pixels are css pixels (96 per inch),
 * so a pixel is {@value #POINTS_PER_PIXEL} points.
 * The svg is rendered as an image (see {@link SvgRenderer}) at {@value #SCALE} times its size (192 dpi),
 * so that it stays sharp when printed or zoomed, with its transparency, and drawn on the whole page.
 */
public class SvgToPdfBinaryStreamer extends TransformBinaryStreamer<SvgImage> {

    static final float POINTS_PER_PIXEL = 72f / 96;

    static final float SCALE = 2;

    public SvgToPdfBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected SvgImage read(final InputStream input) throws IOException {
        return SvgRenderer.render(input.readAllBytes(), SCALE);
    }

    @Override
    protected void write(final SvgImage svg, final OutputStream output) throws IOException {
        float width = svg.svgWidth() * POINTS_PER_PIXEL;
        float height = svg.svgHeight() * POINTS_PER_PIXEL;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);

            PDImageXObject pdfImage = createFromImage(document, svg.image());
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.drawImage(pdfImage, 0, 0, width, height);
            }

            document.save(output);
        }
    }
}
