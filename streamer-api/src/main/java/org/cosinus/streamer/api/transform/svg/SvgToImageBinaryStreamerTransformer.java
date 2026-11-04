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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import java.util.List;

import static javax.imageio.ImageIO.getWriterFormatNames;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.cosinus.streamer.api.transform.svg.SvgFormat.SVG_FORMAT;
import static org.cosinus.swing.image.ImageFormat.writerFormats;

/**
 * {@link BinaryStreamerTransformer} from svg to an image,
 * in any format with an image writer (e.g. png, jpg, bmp).
 * <p>
 * An svg is an image (image/svg+xml) or not, depending on the mime types known, so both are accepted.
 */
@Component
public class SvgToImageBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public SvgToImageBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                      final String targetFormat) {
        return new SvgToImageBinaryStreamer(sourceStreamer.binaryStreamer(), targetFormat);
    }

    @Override
    public List<String> sourceFormats() {
        return List.of(SVG_FORMAT);
    }

    @Override
    public List<String> targetFormats() {
        return writerFormats();
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return SVG_FORMAT.equalsIgnoreCase(sourceStreamer.getType()) &&
            targetFormat != null &&
            contains(getWriterFormatNames(), targetFormat);
    }
}
