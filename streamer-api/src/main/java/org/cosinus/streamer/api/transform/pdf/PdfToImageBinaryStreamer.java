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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

/**
 * {@link TransformBinaryStreamer} that converts pdf content into a single image.
 * <p>
 * All the pages are rendered and stacked from top to bottom, centered, separated by a gray gap.
 * The pages are rendered at {@value #DPI} dpi, or at a lower resolution for long documents,
 * so that the image stays within {@value #MAX_PIXELS} pixels
 * and {@value #MAX_DIMENSION} pixels on each side (the limit of the jpg format).
 * The image has no transparency, so it can be written in any image format (e.g. jpg, bmp).
 */
public class PdfToImageBinaryStreamer extends TransformBinaryStreamer<BufferedImage> {

    static final float DPI = 150;

    static final long MAX_PIXELS = 40_000_000;

    static final int MAX_DIMENSION = 65_000;

    /**
     * Space between two pages, in points.
     */
    static final float GAP = 12;

    static final Color GAP_COLOR = new Color(0xE0E0E0);

    private static final float POINTS_PER_INCH = 72;

    private final String targetFormat;

    public PdfToImageBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(binaryStreamer);
        this.targetFormat = targetFormat;
    }

    @Override
    protected BufferedImage read(final InputStream input) throws IOException {
        try (PDDocument document = Loader.loadPDF(input.readAllBytes())) {
            if (document.getNumberOfPages() == 0) {
                throw new IOException("The pdf document has no page");
            }
            return render(document);
        }
    }

    @Override
    protected void write(final BufferedImage image, final OutputStream output) throws IOException {
        if (!ImageIO.write(image, targetFormat, output)) {
            throw new IOException("No image writer available for format: " + targetFormat);
        }
    }

    private BufferedImage render(final PDDocument document) throws IOException {
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

        float scale = scale(maxWidth, totalHeight, pageCount);
        int imageWidth = pixels(maxWidth, scale);
        int[] pageTops = new int[pageCount];
        int imageHeight = 0;
        for (int index = 0; index < pageCount; index++) {
            pageTops[index] = imageHeight;
            imageHeight += pixels(heights[index], scale) + (index < pageCount - 1 ? pixels(GAP, scale) : 0);
        }
        if (imageHeight > MAX_DIMENSION) {
            throw new IOException("Too many pages to render as a single image: " + pageCount);
        }

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(GAP_COLOR);
            graphics.fillRect(0, 0, imageWidth, imageHeight);
            PDFRenderer renderer = new PDFRenderer(document);
            for (int index = 0; index < pageCount; index++) {
                BufferedImage page = renderer.renderImage(index, scale, ImageType.RGB);
                graphics.drawImage(page, (imageWidth - page.getWidth()) / 2, pageTops[index], null);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    /**
     * The sizes of the pages and gaps are rounded up to whole pixels,
     * so the image can be up to two pixels per page larger than the scaled height, which is kept in reserve.
     *
     * @param width     the width of the widest page, in points
     * @param height    the height of all the pages and gaps, in points
     * @param pageCount the number of pages
     * @return the scale from points to pixels: {@value #DPI} dpi, reduced to keep the image within the limits
     */
    static float scale(final float width, final float height, final int pageCount) {
        double maxHeight = max(1, MAX_DIMENSION - 2.0 * pageCount);
        double scale = DPI / POINTS_PER_INCH;
        scale = min(scale, (MAX_DIMENSION - 1) / width);
        scale = min(scale, maxHeight / height);
        scale = min(scale, sqrt(MAX_PIXELS / ((width + 1.0) * (height + 2.0 * pageCount))) * 0.99);
        return (float) scale;
    }

    /**
     * @return the size in pixels of a length in points, at least one pixel
     */
    private static int pixels(final float points, final float scale) {
        return max(1, (int) ceil(points * scale));
    }
}
