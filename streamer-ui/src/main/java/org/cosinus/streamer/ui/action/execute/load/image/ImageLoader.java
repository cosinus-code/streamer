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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.stream.pipeline.binary.BinaryStreamConsumer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.image.UpdatableImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageLoader extends BinaryStreamConsumer {

    private static final Logger LOG = LogManager.getLogger(ImageLoader.class);

    private final LoadImageWorker loadImageWorker;

    private final Streamer<byte[]> streamerToLoad;

    private final UpdatableImage image;

    public ImageLoader(final LoadImageWorker loadImageWorker) {
        super(new ByteArrayOutputStream());
        this.loadImageWorker = loadImageWorker;
        this.streamerToLoad = loadImageWorker.getStreamerToLoad();
        this.image = new UpdatableImage(streamerToLoad.getType());
    }

    @Override
    public void accept(byte[] item) {
        super.accept(item);
        updateImage();
    }

    private byte[] getImageBytes() {
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    private void updateImage() {
        try {
            image.update(getImageBytes());
            loadImageWorker.publishImage(image);
        } catch (IOException ex) {
            LOG.debug("Failed to load image ({}): {}", ex.getMessage(), streamerToLoad.getPath());
        }
    }
}
