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
package org.cosinus.streamer.api.transform.doc;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer.WORD_FORMATS;
import static org.cosinus.streamer.api.transform.html.HtmlFormat.HTML_FORMATS;

/**
 * {@link BinaryStreamerTransformer} from word (docx, doc or rtf) to html (html or htm).
 */
@Component
public class DocToHtmlBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public DocToHtmlBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                     final String targetFormat) {
        return new DocToHtmlBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            sourceStreamer.getType() != null &&
            WORD_FORMATS.contains(sourceStreamer.getType().toLowerCase()) &&
            targetFormat != null &&
            HTML_FORMATS.contains(targetFormat.toLowerCase());
    }
}
