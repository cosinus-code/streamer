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
package org.cosinus.streamer.api.transform.html;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import java.util.List;

import static javax.imageio.ImageIO.getWriterFormatNames;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTML_FORMAT;
import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTML_FORMATS;
import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTM_FORMAT;
import static org.cosinus.swing.image.ImageFormat.writerFormats;

/**
 * {@link BinaryStreamerTransformer} from html (html or htm) to an image,
 * in any format with an image writer (e.g. png, jpg, bmp).
 */
@Component
public class HtmlToImageBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public HtmlToImageBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                       final String targetFormat) {
        return new HtmlToImageBinaryStreamer(sourceStreamer.binaryStreamer(), targetFormat);
    }

    @Override
    public List<String> sourceFormats() {
        return List.of(HTML_FORMAT, HTM_FORMAT);
    }

    @Override
    public List<String> targetFormats() {
        return writerFormats();
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            sourceStreamer.getType() != null &&
            HTML_FORMATS.contains(sourceStreamer.getType().toLowerCase()) &&
            targetFormat != null &&
            contains(getWriterFormatNames(), targetFormat);
    }
}
