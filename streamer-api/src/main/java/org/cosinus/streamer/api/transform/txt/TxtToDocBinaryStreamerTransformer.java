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

import java.util.Set;

import static org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer.DOCX_FORMAT;
import static org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer.DOC_FORMAT;
import static org.cosinus.streamer.api.transform.txt.TxtFormat.TXT_FORMAT;

/**
 * {@link BinaryStreamerTransformer} from plain text (txt) to word (docx or doc).
 */
@Component
public class TxtToDocBinaryStreamerTransformer implements BinaryStreamerTransformer {

    private static final Set<String> TARGET_FORMATS = Set.of(DOCX_FORMAT, DOC_FORMAT);

    @Override
    public TxtToDocBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                    final String targetFormat) {
        return new TxtToDocBinaryStreamer(sourceStreamer.binaryStreamer(), targetFormat);
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            TXT_FORMAT.equalsIgnoreCase(sourceStreamer.getType()) &&
            targetFormat != null &&
            TARGET_FORMATS.contains(targetFormat.toLowerCase());
    }
}
