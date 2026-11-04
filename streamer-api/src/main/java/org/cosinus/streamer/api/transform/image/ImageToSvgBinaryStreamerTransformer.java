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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.function.Predicate.not;
import static org.cosinus.streamer.api.transform.svg.SvgFormat.SVG_FORMAT;
import static org.cosinus.swing.image.ImageFormat.readerFormats;

/**
 * {@link BinaryStreamerTransformer} from an image (any format with an image reader, except svg) to svg.
 */
@Component
public class ImageToSvgBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public ImageToSvgBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                      final String targetFormat) {
        return new ImageToSvgBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public List<String> sourceFormats() {
        return readerFormats()
            .stream()
            .filter(not(SVG_FORMAT::equals))
            .toList();
    }

    @Override
    public List<String> targetFormats() {
        return List.of(SVG_FORMAT);
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return sourceStreamer.isImage() &&
            !SVG_FORMAT.equalsIgnoreCase(sourceStreamer.getType()) &&
            SVG_FORMAT.equalsIgnoreCase(targetFormat);
    }
}
