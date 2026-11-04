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

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ImageToPdfBinaryStreamerTransformerTest {

    private final ImageToPdfBinaryStreamerTransformer transformer = new ImageToPdfBinaryStreamerTransformer();

    @Test
    public void testCanTransformImageToPdf() {
        assertTrue(transformer.canTransform(image("photo.jpg", new byte[0]), "pdf"));
        assertTrue(transformer.canTransform(image("photo.jpg", new byte[0]), "PDF"));
    }

    @Test
    public void testCannotTransformToOtherFormat() {
        assertFalse(transformer.canTransform(image("photo.jpg", new byte[0]), "png"));
    }

    @Test
    public void testCannotTransformNonImage() {
        assertFalse(transformer.canTransform(new TestBinaryStreamer("notes.txt", new byte[0]), "pdf"));
    }

    @Test
    public void testTransformImageToSinglePagePdfOfImageSize() throws IOException {
        BinaryStreamer source = image("photo.png", imageBytes(120, 80));

        try (PDDocument document = Loader.loadPDF(readAll(transformer.transformStreamer(source, "pdf")))) {
            assertEquals(1, document.getNumberOfPages());
            PDRectangle mediaBox = document.getPage(0).getMediaBox();
            assertEquals(120, mediaBox.getWidth(), 0);
            assertEquals(80, mediaBox.getHeight(), 0);
            assertEquals(1, document.getPage(0).getResources().getXObjectNames().spliterator().estimateSize());
        }
    }

    @Test
    public void testTransformUnreadableImageFails() {
        BinaryStreamer source = image("photo.png", "not an image".getBytes());

        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "pdf")));
    }

    private byte[] imageBytes(int width, int height) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, TYPE_INT_ARGB), "png", output);
        return output.toByteArray();
    }
}
