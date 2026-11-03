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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.image.test.ImageTransformTestApplication;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = ImageTransformTestApplication.class)
public class ImageToImageBinaryStreamerTransformerTest {

    @Autowired
    private ImageToImageBinaryStreamerTransformer transformer;

    @Test
    public void testCanTransformImageToAnotherWritableFormat() {
        assertTrue(transformer.canTransform(image("photo.png", new byte[0]), "jpg"));
        assertTrue(transformer.canTransform(image("photo.png", new byte[0]), "bmp"));
    }

    @Test
    public void testCannotTransformToTheSameFormat() {
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "png"));
        assertFalse(transformer.canTransform(image("photo.PNG", new byte[0]), "png"));
    }

    @Test
    public void testCannotTransformToUnknownFormat() {
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "xyz"));
        assertFalse(transformer.canTransform(image("photo.png", new byte[0]), "pdf"));
    }

    @Test
    public void testCannotTransformNonImage() {
        assertFalse(transformer.canTransform(new TestBinaryStreamer("photo.png", new byte[0]), "jpg"));
    }

    @Test
    public void testTransformImage() throws IOException {
        BinaryStreamer source = image("photo.png", imageBytes(40, 30, "png"));

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(
            readAll(transformer.transformStreamer(source, "bmp"))));

        assertNotNull(result);
        assertEquals(40, result.getWidth());
        assertEquals(30, result.getHeight());
    }

    @Test
    public void testTransformTransparentImageToJpg() throws IOException {
        BinaryStreamer source = image("photo.png", transparentImageBytes());

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(
            readAll(transformer.transformStreamer(source, "jpg"))));

        assertNotNull(result);
        assertEquals(40, result.getWidth());
        assertEquals(30, result.getHeight());
    }

    @Test
    public void testTransformTransparentImageFlattensOnWhite() throws IOException {
        BinaryStreamer source = image("photo.png", transparentImageBytes());

        // bmp is lossless and has no alpha channel, so the exact colors can be checked
        BufferedImage result = ImageIO.read(new ByteArrayInputStream(
            readAll(transformer.transformStreamer(source, "bmp"))));

        assertNotNull(result);
        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0xFF0000, result.getRGB(39, 29) & 0xFFFFFF);
    }

    @Test
    public void testTransformUnreadableImageFails() {
        BinaryStreamer source = image("photo.png", "not an image".getBytes());

        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "bmp")));
    }

    /**
     * @return a 40x30 png, fully transparent except an opaque red bottom right pixel
     */
    private byte[] transparentImageBytes() throws IOException {
        BufferedImage image = new BufferedImage(40, 30, TYPE_INT_ARGB);
        image.setRGB(39, 29, 0xFFFF0000);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }

    static byte[] imageBytes(int width, int height, String format) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, TYPE_INT_RGB), format, output);
        return output.toByteArray();
    }
}
