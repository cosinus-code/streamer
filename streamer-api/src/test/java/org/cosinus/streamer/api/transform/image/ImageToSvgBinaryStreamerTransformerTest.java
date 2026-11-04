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

package org.cosinus.streamer.api.transform.image;

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.svg.SvgRenderer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ImageToSvgBinaryStreamerTransformerTest {

    private static final Pattern DATA_URI = Pattern.compile("xlink:href=\"data:([^;]+);base64,([^\"]+)\"");

    private final ImageToSvgBinaryStreamerTransformer transformer = new ImageToSvgBinaryStreamerTransformer();

    @Test
    public void testCanTransformImageToSvg() {
        assertTrue(transformer.canTransform(image("photo.png", new byte[0]), "svg"));
        assertTrue(transformer.canTransform(image("photo.jpg", new byte[0]), "SVG"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "png"));
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), null));
        assertFalse(transformer.canTransform(image("logo.svg", new byte[0]), "svg"));
        assertFalse(transformer.canTransform(new TestBinaryStreamer("photo.png", new byte[0]), "svg"));
    }

    @Test
    public void testPngIsEmbeddedAsItIs() throws IOException {
        byte[] png = encode(picture(), "png");
        String svg = transform("photo.png", png);

        assertTrue(svg, svg.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<svg "));
        assertTrue(svg, svg.contains("width=\"40\" height=\"20\" viewBox=\"0 0 40 20\""));
        assertEmbedded(svg, "image/png", png);

        BufferedImage rendered = SvgRenderer.render(svg.getBytes(UTF_8), 1).image();
        assertEquals(40, rendered.getWidth());
        assertEquals(20, rendered.getHeight());
        assertEquals(0xFFFF0000, rendered.getRGB(5, 10));
        assertEquals(0xFF0000FF, rendered.getRGB(35, 10));
    }

    @Test
    public void testJpegAndGifAreEmbeddedAsTheyAre() throws IOException {
        byte[] jpeg = encode(picture(), "jpg");
        assertEmbedded(transform("photo.jpg", jpeg), "image/jpeg", jpeg);
        byte[] gif = encode(picture(), "gif");
        assertEmbedded(transform("photo.gif", gif), "image/gif", gif);
    }

    /**
     * A bmp, which the svg viewers don't support, is converted to png.
     */
    @Test
    public void testOtherFormatsAreConvertedToPng() throws IOException {
        String svg = transform("photo.bmp", encode(picture(), "bmp"));
        Matcher dataUri = DATA_URI.matcher(svg);
        assertTrue(svg, dataUri.find());
        assertEquals("image/png", dataUri.group(1));
        BufferedImage embedded = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dataUri.group(2))));
        assertEquals(40, embedded.getWidth());
        assertEquals(0xFFFF0000, embedded.getRGB(5, 10));
    }

    /**
     * The format is detected from the content, not from the extension.
     */
    @Test
    public void testFormatIsDetectedFromTheContent() throws IOException {
        byte[] png = encode(picture(), "png");
        assertEmbedded(transform("photo.jpg", png), "image/png", png);
    }

    @Test
    public void testInvalidContentFails() {
        for (byte[] content : List.of("not an image".getBytes(UTF_8), new byte[0])) {
            TestBinaryStreamer source = image("photo.png", content);
            assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "svg")));
        }
    }

    private void assertEmbedded(String svg, String mimeType, byte[] content) {
        Matcher dataUri = DATA_URI.matcher(svg);
        assertTrue(svg, dataUri.find());
        assertEquals(mimeType, dataUri.group(1));
        assertArrayEquals(content, Base64.getDecoder().decode(dataUri.group(2)));
    }

    /**
     * @return an image 40 x 20, red on the left half, blue on the right half
     */
    private BufferedImage picture() {
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, 20, 20);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(20, 0, 20, 20);
        graphics.dispose();
        return image;
    }

    private byte[] encode(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(format, ImageIO.write(image, format, output));
        return output.toByteArray();
    }

    private String transform(String name, byte[] content) {
        return new String(readAll(transformer.transformStreamer(image(name, content), "svg")), UTF_8);
    }
}
