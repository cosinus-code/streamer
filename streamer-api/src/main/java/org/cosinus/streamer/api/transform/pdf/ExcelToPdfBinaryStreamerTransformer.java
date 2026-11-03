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

package org.cosinus.streamer.api.transform.pdf;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.api.transform.csv.ExcelToCsvBinaryStreamerTransformer.EXCEL_FORMATS;
import static org.cosinus.streamer.api.transform.pdf.ImageToPdfBinaryStreamerTransformer.PDF_FORMAT;

/**
 * {@link BinaryStreamerTransformer} from excel (xlsx or xls) to pdf.
 */
@Component
public class ExcelToPdfBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public ExcelToPdfBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                      final String targetFormat) {
        return new ExcelToPdfBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            sourceStreamer.getType() != null &&
            EXCEL_FORMATS.contains(sourceStreamer.getType().toLowerCase()) &&
            PDF_FORMAT.equalsIgnoreCase(targetFormat);
    }
}
