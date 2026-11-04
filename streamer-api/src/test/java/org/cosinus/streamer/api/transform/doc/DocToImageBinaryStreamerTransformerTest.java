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

package org.cosinus.streamer.api.transform.doc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class DocToImageBinaryStreamerTransformerTest {

    /**
     * The pages are rendered at 150 dpi.
     */
    private static final float SCALE = 150f / 72;

    private final DocToImageBinaryStreamerTransformer transformer = new DocToImageBinaryStreamerTransformer();

    @Test
    public void testCanTransformWordToImage() {
        assertTrue(transformer.canTransform(file("letter.docx"), "png"));
        assertTrue(transformer.canTransform(file("letter.doc"), "jpg"));
        assertTrue(transformer.canTransform(file("LETTER.DOCX"), "PNG"));
        assertTrue(transformer.canTransform(file("letter.rtf"), "png"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("letter.docx"), "pdf"));
        assertFalse(transformer.canTransform(file("letter.docx"), "docx"));
        assertFalse(transformer.canTransform(file("letter.docx"), null));
        assertFalse(transformer.canTransform(file("letter.odt"), "png"));
        assertFalse(transformer.canTransform(file("report.pdf"), "png"));
        assertFalse(transformer.canTransform(file("letter"), "png"));
        assertFalse(transformer.canTransform(image("letter.docx", new byte[0]), "png"));
    }

    @Test
    public void testTransformDocWithTwoPages() throws IOException {
        BufferedImage image = transform("sample.doc", resource("sample.doc"), "png");

        // two portrait A4 pages, stacked
        assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
        assertTrue(image.getHeight() > 2 * pixels(A4.getHeight()));
        // the text of the first page is rendered
        assertTrue(hasDarkPixel(image, 0, pixels(A4.getHeight())));
        // the second page has text too
        assertTrue(hasDarkPixel(image, pixels(A4.getHeight()) + 30, image.getHeight()));
    }

    @Test
    public void testTransformRtfWithTwoPages() throws IOException {
        BufferedImage image = transform("sample.rtf", resource("sample.rtf"), "png");

        assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
        assertTrue(image.getHeight() > 2 * pixels(A4.getHeight()));
        assertTrue(hasDarkPixel(image, 0, pixels(A4.getHeight())));
        assertTrue(hasDarkPixel(image, pixels(A4.getHeight()) + 30, image.getHeight()));
    }

    @Test
    public void testTransformDocxToJpg() throws IOException {
        ByteArrayOutputStream docx = new ByteArrayOutputStream();
        try (XWPFDocument word = new XWPFDocument()) {
            word.createParagraph().createRun().setText("Hello image");
            word.write(docx);
        }

        BufferedImage image = transform("letter.docx", docx.toByteArray(), "jpg");
        assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
        assertEquals(pixels(A4.getHeight()), image.getHeight(), 1);
        assertTrue(hasDarkPixel(image, 0, image.getHeight() / 4));
    }

    /**
     * A pdf file saved with a doc extension (e.g. downloaded from the web) is rendered as pdf,
     * instead of failing with "Unsupported word document format: PDF".
     */
    @Test
    public void testPdfNamedDocIsRendered() throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setNonStrokingColor(0f, 0f, 0f);
                content.addRect(50, A4.getHeight() - 70, 20, 20);
                content.fill();
            }
            document.save(pdf);
        }

        BufferedImage image = transform("craciunul la romani.doc", pdf.toByteArray(), "jpg");
        assertEquals(pixels(A4.getWidth()), image.getWidth(), 1);
        assertEquals(pixels(A4.getHeight()), image.getHeight(), 1);
        assertTrue(hasDarkPixel(image, pixels(55), pixels(65)));
    }

    @Test
    public void testInvalidContentFails() {
        TestBinaryStreamer source = new TestBinaryStreamer("letter.docx", "not a word document".getBytes(UTF_8));
        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "png")));
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

    private BufferedImage transform(String name, byte[] content, String format) throws IOException {
        byte[] bytes = readAll(transformer.transformStreamer(new TestBinaryStreamer(name, content), format));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(format, image);
        return image;
    }

    private byte[] resource(String name) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(name)) {
            assertNotNull(name, input);
            return input.readAllBytes();
        }
    }

    private int pixels(float points) {
        return (int) Math.ceil(points * SCALE);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
