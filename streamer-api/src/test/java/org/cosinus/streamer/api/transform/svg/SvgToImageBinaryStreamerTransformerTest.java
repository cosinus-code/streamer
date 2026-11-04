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

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.image.test.ImageTransformTestApplication;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = ImageTransformTestApplication.class)
public class SvgToImageBinaryStreamerTransformerTest {

    private static final int RED = 0xFFFF0000;

    private static final String RED_RECTANGLE = """
        <svg xmlns="http://www.w3.org/2000/svg" width="200" height="100">
          <rect x="50" y="25" width="100" height="50" fill="red"/>
        </svg>
        """;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Autowired
    private SvgToImageBinaryStreamerTransformer transformer;

    @Test
    public void testCanTransformSvgToImage() {
        assertTrue(transformer.canTransform(file("logo.svg"), "png"));
        assertTrue(transformer.canTransform(file("LOGO.SVG"), "JPG"));
        // an svg file is an image when its mime type is known
        assertTrue(transformer.canTransform(image("logo.svg", new byte[0]), "png"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("logo.svg"), "pdf"));
        assertFalse(transformer.canTransform(file("logo.svg"), "svg"));
        assertFalse(transformer.canTransform(file("logo.svg"), null));
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "jpg"));
        assertFalse(transformer.canTransform(file("logo"), "png"));
    }

    /**
     * The svg is rendered at its size, keeping its transparency in png.
     */
    @Test
    public void testTransformSvgToPng() throws IOException {
        BufferedImage image = transform(RED_RECTANGLE, "png");
        assertEquals(200, image.getWidth());
        assertEquals(100, image.getHeight());
        assertEquals(RED, image.getRGB(100, 50));
        assertEquals(0, image.getRGB(5, 5) >>> 24);
    }

    /**
     * Jpg has no transparency: the svg is rendered on a white background.
     */
    @Test
    public void testTransformSvgToJpg() throws IOException {
        BufferedImage image = transform(RED_RECTANGLE, "jpg");
        assertEquals(200, image.getWidth());
        assertTrue(isNear(Color.WHITE.getRGB(), image.getRGB(5, 5)));
        assertTrue(isNear(RED, image.getRGB(100, 50)));
    }

    @Test
    public void testSizeFromTheViewBox() throws IOException {
        BufferedImage image = transform("""
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 60 30"><circle cx="30" cy="15" r="10"/></svg>
            """, "png");
        assertEquals(60, image.getWidth());
        assertEquals(30, image.getHeight());
    }

    /**
     * An external image (e.g. a local file) is not loaded.
     */
    @Test
    public void testExternalImagesAreNotLoaded() throws IOException {
        File red = folder.newFile("red.png");
        BufferedImage redImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        redImage.getGraphics().setColor(Color.RED);
        redImage.getGraphics().fillRect(0, 0, 10, 10);
        ImageIO.write(redImage, "png", red);
        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="20" height="20">
              <image x="0" y="0" width="20" height="20" xlink:href="%s"/>
            </svg>
            """.formatted(red.toURI());

        assertNoRedOrFails(svg);
    }

    /**
     * An external entity (e.g. a local file) is not loaded.
     */
    @Test
    public void testExternalEntitiesAreNotLoaded() throws IOException {
        File width = folder.newFile("width.txt");
        Files.writeString(width.toPath(), "300");
        String svg = """
            <?xml version="1.0"?>
            <!DOCTYPE svg [<!ENTITY width SYSTEM "%s">]>
            <svg xmlns="http://www.w3.org/2000/svg" width="&width;0" height="20"/>
            """.formatted(width.toURI());

        try {
            assertNotEquals(3000, transform(svg, "png").getWidth());
        } catch (UncheckedIOException e) {
            // failing is fine too
        }
    }

    /**
     * The scripts are not executed.
     */
    @Test
    public void testScriptsAreNotExecuted() throws IOException {
        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                 onload="document.getElementById('r').setAttribute('fill', 'blue')">
              <rect id="r" width="20" height="20" fill="red"/>
              <script type="text/ecmascript">document.getElementById('r').setAttribute('fill', 'blue');</script>
            </svg>
            """;
        assertEquals(RED, transform(svg, "png").getRGB(10, 10));
    }

    @Test
    public void testInvalidContentFails() {
        for (String content : List.of("<svg><unclosed></svg>", "not svg", "")) {
            TestBinaryStreamer source = new TestBinaryStreamer("logo.svg", content.getBytes(UTF_8));
            assertThrows(content, UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "png")));
        }
    }

    private void assertNoRedOrFails(String svg) throws IOException {
        BufferedImage image;
        try {
            image = transform(svg, "png");
        } catch (UncheckedIOException e) {
            return;
        }
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                assertNotEquals("pixel " + x + "," + y, RED, image.getRGB(x, y));
            }
        }
    }

    private boolean isNear(int expected, int actual) {
        for (int shift = 0; shift < 24; shift += 8) {
            if (Math.abs((expected >> shift & 0xFF) - (actual >> shift & 0xFF)) > 24) {
                return false;
            }
        }
        return true;
    }

    private BufferedImage transform(String svg, String format) throws IOException {
        byte[] bytes = readAll(transformer.transformStreamer(new TestBinaryStreamer("logo.svg", svg.getBytes(UTF_8)), format));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        assertNotNull(format, image);
        return image;
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
