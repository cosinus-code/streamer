/*
 * Copyright 2025 Cosinus Software
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

package org.cosinus.streamer.ui.action.execute.load.image;

import lombok.extern.slf4j.Slf4j;
import org.cosinus.stream.binary.BinaryStream;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.UpdatableImage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;
import static org.cosinus.streamer.ui.action.execute.load.image.LoadImageWorker.IMAGE_LOAD_RATE;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Loader of an image from its bytes, received in chunks:
 * the image is updated with all the bytes received so far, after each chunk.
 */
@Slf4j
public class ImageLoader {

    public static final long PARTIAL_DECODE_INTERVAL = 1000;

    @Autowired
    private ImageHandler imageHandler;

    private final Streamer<byte[]> streamerToLoad;

    private final ByteArrayOutputStream imageBytesOutput;

    private final UpdatableImage image;

    private boolean showPartialImage = true;

    public ImageLoader(final Streamer<byte[]> streamerToLoad) {
        injectContext(this);
        this.streamerToLoad = streamerToLoad;
        this.imageBytesOutput = new ByteArrayOutputStream();
        this.image = new UpdatableImage(streamerToLoad.getType());
    }

    public Stream<UpdatableImage> stream() {
        return BinaryStream.of(streamerToLoad.binaryStreamer().inputStream(), IMAGE_LOAD_RATE)
            .map(this::updateAndGet);
    }

    /**
     * Update the image with the next chunk of bytes.
     *
     * @param bytes the next bytes of the image
     * @return the updated image, or empty if the bytes received so far cannot be loaded as image yet
     */
    public UpdatableImage updateAndGet(byte[] bytes) {
        imageBytesOutput.writeBytes(bytes);
        byte[] imageBytes = imageBytesOutput.toByteArray();
        try {
            if (showPartialImage || isComplete()) {
                long startTime = currentTimeMillis();
                image.update(imageBytes);
                if (!isComplete() && currentTimeMillis() - startTime > PARTIAL_DECODE_INTERVAL) {
                    showPartialImage = false;
                }
            }
        } catch (IOException ex) {
            if (!isComplete() && !imageHandler.isJpeg(imageBytes)) {
                showPartialImage = false;
            }
        } finally {
            image.setSize(imageBytes.length);
        }
        return image;
    }

    private boolean isComplete() {
        return imageBytesOutput.size() >= streamerToLoad.getSize();
    }
}
