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

package org.cosinus.streamer.api.transform.excel;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import static org.cosinus.streamer.api.transform.csv.CsvFormat.CSV_FORMAT;
import static org.cosinus.streamer.api.transform.excel.ExcelFormat.EXCEL_FORMATS;

/**
 * {@link BinaryStreamerTransformer} from excel (xlsx or xls) to csv.
 */
@Component
public class ExcelToCsvBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public ExcelToCsvBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                      final String targetFormat) {
        return new ExcelToCsvBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return sourceStreamer.getType() != null &&
            EXCEL_FORMATS.contains(sourceStreamer.getType().toLowerCase()) &&
            CSV_FORMAT.equalsIgnoreCase(targetFormat);
    }
}
