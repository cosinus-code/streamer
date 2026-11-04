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
package org.cosinus.streamer.api.transform.txt;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTML_FORMAT;
import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTML_FORMATS;
import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTM_FORMAT;
import static org.cosinus.streamer.api.transform.txt.TxtFormat.TXT_FORMAT;

/**
 * {@link BinaryStreamerTransformer} from plain text (txt) to html (html or htm).
 */
@Component
public class TxtToHtmlBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public TxtToHtmlBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                     final String targetFormat) {
        return new TxtToHtmlBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public List<String> sourceFormats() {
        return List.of(TXT_FORMAT);
    }

    @Override
    public List<String> targetFormats() {
        return List.of(HTML_FORMAT, HTM_FORMAT);
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            TXT_FORMAT.equalsIgnoreCase(sourceStreamer.getType()) &&
            targetFormat != null &&
            HTML_FORMATS.contains(targetFormat.toLowerCase());
    }
}
