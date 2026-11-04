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

package org.cosinus.streamer.api.transform.svg;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SvgToPdfBinaryStreamerTransformerTest {

    private static final String RED_RECTANGLE = """
        <svg xmlns="http://www.w3.org/2000/svg" width="200" height="100">
          <rect x="50" y="25" width="100" height="50" fill="red"/>
        </svg>
        """;

    private final SvgToPdfBinaryStreamerTransformer transformer = new SvgToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformSvgToPdf() {
        assertTrue(transformer.canTransform(file("logo.svg"), "pdf"));
        assertTrue(transformer.canTransform(file("LOGO.SVG"), "PDF"));
        // an svg file is an image when its mime type is known
        assertTrue(transformer.canTransform(image("logo.svg", new byte[0]), "pdf"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("logo.svg"), "png"));
        assertFalse(transformer.canTransform(file("logo.svg"), null));
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "pdf"));
        assertFalse(transformer.canTransform(file("logo"), "pdf"));
    }

    /**
     * The page has the printed size of the svg (a css pixel is 0.75 points),
     * and the svg is rendered at twice its size, to stay sharp.
     */
    @Test
    public void testTransformSvg() throws IOException {
        try (PDDocument pdf = transform(RED_RECTANGLE)) {
            assertEquals(1, pdf.getNumberOfPages());
            PDPage page = pdf.getPage(0);
            assertEquals(150, page.getMediaBox().getWidth(), 0.01);
            assertEquals(75, page.getMediaBox().getHeight(), 0.01);

            PDImageXObject image = (PDImageXObject) page.getResources()
                .getXObject(page.getResources().getXObjectNames().iterator().next());
            assertEquals(400, image.getWidth());
            assertEquals(200, image.getHeight());
            // the transparency is kept, as a soft mask
            assertTrue(image.getCOSObject().containsKey(COSName.SMASK));

            BufferedImage rendered = new PDFRenderer(pdf).renderImageWithDPI(0, 72);
            assertEquals(0xFFFF0000, rendered.getRGB(75, 37));
            assertEquals(0xFFFFFFFF, rendered.getRGB(5, 5));
        }
    }

    @Test
    public void testSizeFromTheViewBox() throws IOException {
        try (PDDocument pdf = transform("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 80 40"><circle cx="40" cy="20" r="10"/></svg>
            """)) {
            assertEquals(60, pdf.getPage(0).getMediaBox().getWidth(), 0.01);
            assertEquals(30, pdf.getPage(0).getMediaBox().getHeight(), 0.01);
        }
    }

    @Test
    public void testInvalidContentFails() {
        for (String content : List.of("<svg><unclosed></svg>", "not svg", "")) {
            TestBinaryStreamer source = new TestBinaryStreamer("logo.svg", content.getBytes(UTF_8));
            assertThrows(content, UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "pdf")));
        }
    }

    private PDDocument transform(String svg) throws IOException {
        return Loader.loadPDF(readAll(transformer.transformStreamer(
            new TestBinaryStreamer("logo.svg", svg.getBytes(UTF_8)), "pdf")));
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
