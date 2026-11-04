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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.svg.SvgRenderer;
import org.junit.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PdfToSvgBinaryStreamerTransformerTest {

    private static final PDRectangle PAGE = new PDRectangle(300, 150);

    private final PdfToSvgBinaryStreamerTransformer transformer = new PdfToSvgBinaryStreamerTransformer();

    @Test
    public void testCanTransformPdfToSvg() {
        assertTrue(transformer.canTransform(file("report.pdf"), "svg"));
        assertTrue(transformer.canTransform(file("REPORT.PDF"), "SVG"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("report.pdf"), "png"));
        assertFalse(transformer.canTransform(file("report.pdf"), null));
        assertFalse(transformer.canTransform(file("logo.svg"), "svg"));
        assertFalse(transformer.canTransform(file("report"), "svg"));
        assertFalse(transformer.canTransform(image("report.pdf", new byte[0]), "svg"));
    }

    /**
     * The page is drawn as vectors, at the printed size (a point is 4/3 css pixels).
     */
    @Test
    public void testTransformPdf() throws IOException {
        String svg = transform(pdf(1, false));
        assertTrue(svg, svg.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(svg, svg.contains("<svg"));
        assertTrue(svg, svg.contains("width=\"400\""));
        assertTrue(svg, svg.contains("height=\"200\""));
        assertTrue(svg, svg.contains("viewBox=\"0 0 400 200\""));
        // the text is drawn as the outlines of its glyphs, and there is no image
        assertTrue(svg.contains("<path"));
        assertFalse(svg.contains("<image"));

        BufferedImage image = SvgRenderer.render(svg.getBytes(UTF_8), 1).image();
        assertEquals(400, image.getWidth());
        assertEquals(200, image.getHeight());
        // the red rectangle (from 20,20 to 120,70 points, from the bottom) and the white page
        assertTrue(isNear(0xFFFF0000, image.getRGB(80, 130)));
        assertTrue(isNear(0xFFFFFFFF, image.getRGB(390, 190)));
    }

    /**
     * The pages are stacked from top to bottom, separated by a gray gap.
     */
    @Test
    public void testPagesAreStacked() throws IOException {
        String svg = transform(pdf(2, false));
        // two pages of 150 points and a gap of 12 points
        assertTrue(svg, svg.contains("height=\"416\""));

        BufferedImage image = SvgRenderer.render(svg.getBytes(UTF_8), 1).image();
        assertTrue(isNear(0xFFFF0000, image.getRGB(80, 130)));
        assertTrue(isNear(0xFFE0E0E0, image.getRGB(200, 208)));
        assertTrue(isNear(0xFFFF0000, image.getRGB(80, 130 + 216)));
    }

    /**
     * An image of the pdf is embedded in the svg, so it is shown even when no external resource is loaded.
     */
    @Test
    public void testImagesAreEmbedded() throws IOException {
        String svg = transform(pdf(1, true));
        assertTrue(svg, svg.contains("data:image/png;base64,"));

        BufferedImage image = SvgRenderer.render(svg.getBytes(UTF_8), 1).image();
        // the blue image, from 200,50 to 280,130 points
        assertTrue(isNear(0xFF0000FF, image.getRGB(320, 60)));
    }

    @Test
    public void testInvalidContentFails() throws IOException {
        ByteArrayOutputStream empty = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            document.save(empty);
        }
        for (byte[] content : List.of("not a pdf".getBytes(UTF_8), new byte[0], empty.toByteArray())) {
            TestBinaryStreamer source = new TestBinaryStreamer("report.pdf", content);
            assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "svg")));
        }
    }

    private byte[] pdf(int pageCount, boolean withImage) throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            for (int index = 0; index < pageCount; index++) {
                PDPage page = new PDPage(PAGE);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.setNonStrokingColor(Color.RED);
                    content.addRect(20, 20, 100, 50);
                    content.fill();
                    content.setNonStrokingColor(Color.BLACK);
                    content.beginText();
                    content.setFont(new PDType1Font(HELVETICA), 14);
                    content.newLineAtOffset(20, 120);
                    content.showText("Page " + (index + 1));
                    content.endText();
                    if (withImage) {
                        BufferedImage blue = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics = blue.createGraphics();
                        graphics.setColor(Color.BLUE);
                        graphics.fillRect(0, 0, 8, 8);
                        graphics.dispose();
                        content.drawImage(LosslessFactory.createFromImage(document, blue), 200, 50, 80, 80);
                    }
                }
            }
            document.save(pdf);
        }
        return pdf.toByteArray();
    }

    private boolean isNear(int expected, int actual) {
        for (int shift = 0; shift < 32; shift += 8) {
            if (Math.abs((expected >> shift & 0xFF) - (actual >> shift & 0xFF)) > 24) {
                return false;
            }
        }
        return true;
    }

    private String transform(byte[] pdf) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer("report.pdf", pdf), "svg")), UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
