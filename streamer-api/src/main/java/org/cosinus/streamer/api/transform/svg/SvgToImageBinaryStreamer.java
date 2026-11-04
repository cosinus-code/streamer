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

package org.cosinus.streamer.api.transform.svg;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.image.ImageToImageBinaryStreamer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link TransformBinaryStreamer} that converts svg content into an image,
 * in any format with an image writer (e.g. png, jpg, bmp).
 * <p>
 * The svg is rendered at its own size (see {@link SvgRenderer}),
 * keeping its transparency when the target format has an alpha channel (e.g. png),
 * or on a white background otherwise (see {@link ImageToImageBinaryStreamer}).
 */
public class SvgToImageBinaryStreamer extends ImageToImageBinaryStreamer {

    public SvgToImageBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(binaryStreamer, targetFormat);
    }

    @Override
    protected BufferedImage read(final InputStream input) throws IOException {
        return SvgRenderer.render(input.readAllBytes(), 1).image();
    }
}
