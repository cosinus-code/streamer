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

package org.cosinus.streamer.api.transform.html;

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HtmlToImageBinaryStreamerTransformerTest {

    /**
     * The pages are rendered at 150 dpi.
     */
    private static final float SCALE = 150f / 72;

    private final HtmlToImageBinaryStreamerTransformer transformer = new HtmlToImageBinaryStreamerTransformer();

    @Test
    public void testCanTransformHtmlToImage() {
        assertTrue(transformer.canTransform(file("page.html"), "png"));
        assertTrue(transformer.canTransform(file("page.htm"), "jpg"));
        assertTrue(transformer.canTransform(file("PAGE.HTML"), "PNG"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("page.html"), "pdf"));
        assertFalse(transformer.canTransform(file("page.html"), "txt"));
        assertFalse(transformer.canTransform(file("page.html"), null));
        assertFalse(transformer.canTransform(file("page.xhtml"), "png"));
        assertFalse(transformer.canTransform(file("page.txt"), "png"));
        assertFalse(transformer.canTransform(file("page"), "png"));
        assertFalse(transformer.canTransform(image("page.html", new byte[0]), "png"));
    }

    @Test
    public void testTransformHtmlToPng() throws IOException {
        BufferedImage image = transform("<html><body><h1>Introducere</h1><p>Ștefan din Iași</p></body></html>", "png");

        assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
        assertEquals(pixels(A4.getHeight()), image.getHeight(), 1);
        // the text is at the top of the page
        assertTrue(hasDarkPixel(image, 0, image.getHeight() / 10));
        assertFalse(hasDarkPixel(image, image.getHeight() / 2, image.getHeight()));
    }

    /**
     * A page break starts a new page: the two pages are stacked, each one with its text.
     */
    @Test
    public void testPageBreakGivesTwoStackedPages() throws IOException {
        BufferedImage image = transform(
            "<p>page one</p><p style=\"page-break-before: always\">page two</p>", "jpg");

        int pageHeight = pixels(A4.getHeight());
        assertTrue(image.getHeight() > 2 * pageHeight);
        assertTrue(hasDarkPixel(image, 0, pageHeight / 10));
        assertTrue(hasDarkPixel(image, pageHeight + 10, pageHeight + pageHeight / 10));
    }

    @Test
    public void testEmptyHtmlGivesAnEmptyPage() throws IOException {
        BufferedImage image = transform("", "png");
        assertEquals(pixels(A4.getHeight()), image.getHeight(), 1);
        assertFalse(hasDarkPixel(image, 0, image.getHeight()));
    }

    private boolean hasDarkPixel(BufferedImage image, int fromY, int toY) {
        for (int y = fromY; y < toY; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                if ((rgb & 0xFF) < 0x40 && (rgb >> 8 & 0xFF) < 0x40 && (rgb >> 16 & 0xFF) < 0x40) {
                    return true;
                }
            }
        }
        return false;
    }

    private BufferedImage transform(String html, String format) throws IOException {
        byte[] bytes = readAll(transformer.transformStreamer(
            new TestBinaryStreamer("page.html", html.getBytes(UTF_8)), format));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(format, image);
        return image;
    }

    private int pixels(float points) {
        return (int) Math.ceil(points * SCALE);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
