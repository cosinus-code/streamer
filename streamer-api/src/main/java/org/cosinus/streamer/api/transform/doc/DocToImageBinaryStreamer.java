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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer;

/**
 * {@link TransformBinaryStreamer} that converts word content (docx, doc or rtf) into a single image.
 * <p>
 * The document is first converted to pdf (see {@link DocToPdfBinaryStreamer}),
 * then its pages are rendered and stacked in one image (see {@link PdfToImageBinaryStreamer}).
 */
public class DocToImageBinaryStreamer extends PdfToImageBinaryStreamer {

    public DocToImageBinaryStreamer(final BinaryStreamer binaryStreamer, final String targetFormat) {
        super(new DocToPdfBinaryStreamer(binaryStreamer), targetFormat);
    }
}
