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

import java.util.List;
import java.util.Set;

import static org.cosinus.streamer.api.transform.pdf.PdfFormat.PDF_FORMAT;

/**
 * {@link BinaryStreamerTransformer} from word (docx, doc or rtf) to pdf.
 */
@Component
public class DocToPdfBinaryStreamerTransformer implements BinaryStreamerTransformer {

    public static final String DOCX_FORMAT = "docx";

    public static final String DOC_FORMAT = "doc";

    public static final String RTF_FORMAT = "rtf";

    public static final Set<String> WORD_FORMATS = Set.of(DOCX_FORMAT, DOC_FORMAT, RTF_FORMAT);

    @Override
    public DocToPdfBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                    final String targetFormat) {
        return new DocToPdfBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public List<String> sourceFormats() {
        return List.of(DOCX_FORMAT, DOC_FORMAT, RTF_FORMAT);
    }

    @Override
    public List<String> targetFormats() {
        return List.of(PDF_FORMAT);
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            sourceStreamer.getType() != null &&
            WORD_FORMATS.contains(sourceStreamer.getType().toLowerCase()) &&
            PDF_FORMAT.equalsIgnoreCase(targetFormat);
    }
}
