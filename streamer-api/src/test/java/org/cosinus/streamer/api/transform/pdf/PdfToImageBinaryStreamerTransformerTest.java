/*
 * Copyright 2026 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.api.transform.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.IntFunction;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A5;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.DPI;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.GAP;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.GAP_COLOR;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.MAX_DIMENSION;
import static org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer.MAX_PIXELS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PdfToImageBinaryStreamerTransformerTest {

    private static final int WHITE = 0xFFFFFF;

    private static final int BLACK = 0x000000;

    private static final float SCALE = DPI / 72;

    private final PdfToImageBinaryStreamerTransformer transformer = new PdfToImageBinaryStreamerTransformer();

    @Test
    public void testCanTransformPdfToImage() {
        assertTrue(transformer.canTransform(file("report.pdf"), "png"));
        assertTrue(transformer.canTransform(file("report.pdf"), "jpg"));
        assertTrue(transformer.canTransform(file("report.pdf"), "bmp"));
        assertTrue(transformer.canTransform(file("REPORT.PDF"), "PNG"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("report.pdf"), "pdf"));
        assertFalse(transformer.canTransform(file("report.pdf"), "docx"));
        assertFalse(transformer.canTransform(file("report.pdf"), null));
        assertFalse(transformer.canTransform(file("report.docx"), "png"));
        assertFalse(transformer.canTransform(file("report"), "png"));
        assertFalse(transformer.canTransform(image("report.pdf", new byte[0]), "png"));
    }

    @Test
    public void testPagesAreStackedWithAGap() throws IOException {
        // first page with a black square in its top-left corner, second page empty
        BufferedImage image = transform(pdf(2, index -> A4, true), "png");

        int pageHeight = pixels(A4.getHeight());
        assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
        assertEquals(2 * pageHeight + pixels(GAP), image.getHeight(), 2);

        int center = image.getWidth() / 2;
        assertEquals(BLACK, rgb(image, pixels(60), pixels(60)));
        assertEquals(WHITE, rgb(image, center, pageHeight / 2));
        assertEquals(GAP_COLOR.getRGB() & WHITE, rgb(image, center, pageHeight + pixels(GAP) / 2));
        assertEquals(WHITE, rgb(image, center, pageHeight + pixels(GAP) + pageHeight / 2));
    }

    @Test
    public void testNarrowerPagesAreCentered() throws IOException {
        BufferedImage image = transform(pdf(2, index -> index == 0 ? A4 : A5, false), "png");

        int top = pixels(A4.getHeight()) + pixels(GAP);
        int margin = (pixels(A4.getWidth()) - pixels(A5.getWidth())) / 2;
        assertEquals(GAP_COLOR.getRGB() & WHITE, rgb(image, margin / 2, top + 10));
        assertEquals(WHITE, rgb(image, image.getWidth() / 2, top + 10));
        assertEquals(GAP_COLOR.getRGB() & WHITE, rgb(image, image.getWidth() - margin / 2, top + 10));
    }

    @Test
    public void testRotatedPageIsLandscape() throws IOException {
        byte[] pdf;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(A4);
            page.setRotation(90);
            document.addPage(page);
            pdf = save(document);
        }
        BufferedImage image = transform(pdf, "png");
        assertEquals(pixels(A4.getHeight()), image.getWidth(), 1);
        assertEquals(pixels(A4.getWidth()), image.getHeight(), 1);
    }

    @Test
    public void testTransformToJpgAndBmp() throws IOException {
        byte[] pdf = pdf(1, index -> A4, true);
        for (String format : new String[]{"jpg", "bmp"}) {
            BufferedImage image = transform(pdf, format);
            assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
            // jpg is lossy: the colors are only close
            int rgb = rgb(image, pixels(60), pixels(60));
            assertTrue(format, (rgb & 0xFF) < 0x20);
        }
    }

    @Test
    public void testLongDocumentStaysWithinTheLimits() throws IOException {
        BufferedImage image = transform(pdf(200, index -> A4, false), "jpg");

        assertTrue(image.getHeight() <= MAX_DIMENSION);
        assertTrue(image.getWidth() <= MAX_DIMENSION);
        assertTrue((long) image.getWidth() * image.getHeight() <= MAX_PIXELS);
        // the resolution is reduced, but the pages keep their proportions
        assertTrue(image.getWidth() < pixels(A4.getWidth()));
        assertTrue(image.getHeight() > 200 * image.getWidth());
    }

    @Test
    public void testScale() {
        assertEquals(SCALE, PdfToImageBinaryStreamer.scale(A4.getWidth(), A4.getHeight(), 1), 0.001);

        // the rounding of 1000 pages and gaps is kept in reserve
        float tall = 1000 * A4.getHeight();
        float scale = PdfToImageBinaryStreamer.scale(A4.getWidth(), tall, 1000);
        assertTrue(scale * tall + 2 * 1000 <= MAX_DIMENSION);

        float huge = 20_000;
        scale = PdfToImageBinaryStreamer.scale(huge, huge, 1);
        assertTrue((scale * huge + 1) * (scale * huge + 2) <= MAX_PIXELS);
    }

    @Test
    public void testInvalidContentFails() throws IOException {
        assertTransformFails("not a pdf".getBytes(UTF_8));
        try (PDDocument document = new PDDocument()) {
            assertTransformFails(save(document));
        }
    }

    private void assertTransformFails(byte[] content) {
        TestBinaryStreamer source = new TestBinaryStreamer("report.pdf", content);
        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "png")));
    }

    /**
     * @param pageCount   the number of pages
     * @param pageSize    the size of each page
     * @param blackSquare true to draw a black square at 50-70 points from the top-left corner of the first page
     * @return the pdf content
     */
    private byte[] pdf(int pageCount, IntFunction<PDRectangle> pageSize, boolean blackSquare) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (int index = 0; index < pageCount; index++) {
                PDRectangle size = pageSize.apply(index);
                PDPage page = new PDPage(size);
                document.addPage(page);
                if (index == 0 && blackSquare) {
                    try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                        content.setNonStrokingColor(0f, 0f, 0f);
                        content.addRect(50, size.getHeight() - 70, 20, 20);
                        content.fill();
                    }
                }
            }
            return save(document);
        }
    }

    private byte[] save(PDDocument document) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.save(output);
        return output.toByteArray();
    }

    private BufferedImage transform(byte[] pdf, String format) throws IOException {
        byte[] content = readAll(transformer.transformStreamer(new TestBinaryStreamer("report.pdf", pdf), format));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
        assertNotNull(format, image);
        return image;
    }

    private int pixels(float points) {
        return (int) Math.ceil(points * SCALE);
    }

    private int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & WHITE;
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
