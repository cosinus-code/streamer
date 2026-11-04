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

package org.cosinus.streamer.api.transform.image;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * {@link TransformBinaryStreamer} that converts an image into an svg image, embedding it.
 * <p>
 * The image is embedded in the svg as a data uri, at its size (one image pixel per svg pixel):
 * a png, jpeg or gif image as it is (no quality loss, no size growth),
 * any other image (e.g. bmp, tiff), which the svg viewers don't support, converted to png.
 * The format is detected from the content of the image, not from its extension.
 * The image is not traced into vector shapes.
 */
public class ImageToSvgBinaryStreamer extends TransformBinaryStreamer<ImageToSvgBinaryStreamer.EmbeddedImage> {

    public static final String SVG_CONTENT = """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" \
        width="%1$d" height="%2$d" viewBox="0 0 %1$d %2$d">
          <image width="%1$d" height="%2$d" xlink:href="data:%3$s;base64,%4$s"/>
        </svg>
        """;
    /**
     * The mime types of the image formats embedded as they are, by format name of the image readers.
     */
    static final Map<String, String> EMBEDDED_FORMATS = Map.of(
        "png", "image/png",
        "jpeg", "image/jpeg",
        "gif", "image/gif");

    private static final String PNG_MIME_TYPE = "image/png";

    public ImageToSvgBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    /**
     * An image to embed.
     *
     * @param mimeType the mime type of the image content
     * @param content  the image content
     * @param width    the width of the image, in pixels
     * @param height   the height of the image, in pixels
     */
    record EmbeddedImage(String mimeType, byte[] content, int width, int height) {
    }

    @Override
    protected EmbeddedImage read(final InputStream input) throws IOException {
        byte[] content = input.readAllBytes();
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = imageInput != null ? ImageIO.getImageReaders(imageInput) : null;
            if (readers == null || !readers.hasNext()) {
                throw new IOException("Unsupported or invalid image content");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput);
                String mimeType = EMBEDDED_FORMATS.get(reader.getFormatName().toLowerCase());
                if (mimeType != null) {
                    return new EmbeddedImage(mimeType, content, reader.getWidth(0), reader.getHeight(0));
                }
                BufferedImage image = reader.read(0);
                return new EmbeddedImage(PNG_MIME_TYPE, png(image), image.getWidth(), image.getHeight());
            } finally {
                reader.dispose();
            }
        }
    }

    @Override
    protected void write(final EmbeddedImage image, final OutputStream output) throws IOException {
        String svg = SVG_CONTENT.formatted(image.width(), image.height(), image.mimeType(),
            Base64.getEncoder().encodeToString(image.content()));
        output.write(svg.getBytes(UTF_8));
    }

    private byte[] png(final BufferedImage image) throws IOException {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", png)) {
            throw new IOException("No png image writer available");
        }
        return png.toByteArray();
    }
}
