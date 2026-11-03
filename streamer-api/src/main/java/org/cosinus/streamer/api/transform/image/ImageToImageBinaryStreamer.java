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
import org.cosinus.swing.image.ImageHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static javax.imageio.ImageIO.getImageWriters;
import static javax.imageio.ImageTypeSpecifier.createFromRenderedImage;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * {@link TransformBinaryStreamer} that converts an image into another image format.
 * <p>
 * If the target format cannot encode the image as it is
 * (e.g. an image with transparency converted to jpg or bmp, which have no alpha channel),
 * the image is first flattened on a white background.
 */
public class ImageToImageBinaryStreamer extends TransformBinaryStreamer<BufferedImage> {

    private final String targetFormat;

    @Autowired
    private ImageHandler imageHandler;

    public ImageToImageBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(binaryStreamer);
        injectContext(this);
        this.targetFormat = targetFormat;
    }

    @Override
    protected BufferedImage read(InputStream input) throws IOException {
        return ImageIO.read(input);
    }

    @Override
    protected void write(BufferedImage image, OutputStream output) throws IOException {
        BufferedImage imageToWrite = canWrite(image) ? image : imageHandler.flatten(image);
        if (!ImageIO.write(imageToWrite, targetFormat, output)) {
            throw new IOException("No image writer available for format: " + targetFormat);
        }
    }

    private boolean canWrite(BufferedImage image) {
        return getImageWriters(createFromRenderedImage(image), targetFormat).hasNext();
    }
}
