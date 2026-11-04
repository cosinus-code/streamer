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

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.SVG_SVG_TAG;
import static org.apache.batik.util.SVGConstants.SVG_VIEW_BOX_ATTRIBUTE;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.GAP;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.GAP_COLOR;

/**
 * {@link TransformBinaryStreamer} that converts pdf content into a single svg image, as vector graphics.
 * <p>
 * All the pages are drawn and stacked from top to bottom, centered, separated by a gray gap,
 * as for the images (see {@link PdfToImageBinaryStreamer}), each page on a white background.
 * The pages are drawn with pdfbox on an svg graphics (batik), so the shapes and the text stay vectors
 * (the text as the outlines of its glyphs), and the images of the pdf are embedded in the svg (as png).
 * <p>
 * The svg has the printed size of the pdf: the svg pixels are css pixels (96 per inch),
 * so a point is {@value #PIXELS_PER_POINT} pixels.
 */
public class PdfToSvgBinaryStreamer extends TransformBinaryStreamer<PdfToSvgBinaryStreamer.Svg> {

    static final float PIXELS_PER_POINT = 96f / 72;

    public PdfToSvgBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    /**
     * The svg drawn, with its root element.
     */
    record Svg(SVGGraphics2D graphics, Element root) {
    }

    @Override
    protected Svg read(final InputStream input) throws IOException {
        try (PDDocument document = Loader.loadPDF(input.readAllBytes())) {
            if (document.getNumberOfPages() == 0) {
                throw new IOException("The pdf document has no page");
            }
            return draw(document);
        }
    }

    @Override
    protected void write(final Svg svg, final OutputStream output) throws IOException {
        // the xml declaration and the svg doctype are written by the svg graphics
        Writer writer = new OutputStreamWriter(output, UTF_8);
        svg.graphics().stream(svg.root(), writer, true, false);
        writer.flush();
    }

    private Svg draw(final PDDocument document) throws IOException {
        int pageCount = document.getNumberOfPages();
        float[] widths = new float[pageCount];
        float[] heights = new float[pageCount];
        float maxWidth = 0;
        float totalHeight = GAP * (pageCount - 1);
        for (int index = 0; index < pageCount; index++) {
            PDPage page = document.getPage(index);
            PDRectangle box = page.getCropBox();
            boolean rotated = page.getRotation() % 180 != 0;
            widths[index] = rotated ? box.getHeight() : box.getWidth();
            heights[index] = rotated ? box.getWidth() : box.getHeight();
            maxWidth = max(maxWidth, widths[index]);
            totalHeight += heights[index];
        }

        Document dom = GenericDOMImplementation.getDOMImplementation()
            .createDocument(SVG_NAMESPACE_URI, SVG_SVG_TAG, null);
        SVGGeneratorContext context = SVGGeneratorContext.createDefault(dom);
        context.setComment(null);
        SVGGraphics2D graphics = new SVGGraphics2D(context, false);
        graphics.setSVGCanvasSize(new Dimension(
            (int) ceil(maxWidth * PIXELS_PER_POINT), (int) ceil(totalHeight * PIXELS_PER_POINT)));
        graphics.scale(PIXELS_PER_POINT, PIXELS_PER_POINT);

        if (pageCount > 1) {
            graphics.setColor(GAP_COLOR);
            graphics.fill(new Rectangle2D.Float(0, 0, maxWidth, totalHeight));
        }
        PDFRenderer renderer = new PDFRenderer(document);
        float top = 0;
        for (int index = 0; index < pageCount; index++) {
            Graphics2D pageGraphics = (Graphics2D) graphics.create();
            try {
                pageGraphics.translate((maxWidth - widths[index]) / 2, top);
                pageGraphics.setColor(Color.WHITE);
                pageGraphics.fill(new Rectangle2D.Float(0, 0, widths[index], heights[index]));
                pageGraphics.clip(new Rectangle2D.Float(0, 0, widths[index], heights[index]));
                renderer.renderPageToGraphics(index, pageGraphics, 1);
            } finally {
                pageGraphics.dispose();
            }
            top += heights[index] + GAP;
        }

        Element root = graphics.getRoot();
        root.setAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE, "0 0 %s %s".formatted(
            root.getAttributeNS(null, "width"), root.getAttributeNS(null, "height")));
        return new Svg(graphics, root);
    }
}
