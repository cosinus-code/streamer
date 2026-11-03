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

package org.cosinus.streamer.api.transform.csv;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

/**
 * {@link BinaryStreamerTransformer} from csv to xlsx.
 */
@Component
public class CsvToExcelBinaryStreamerTransformer implements BinaryStreamerTransformer {

    public static final String CSV_FORMAT = "csv";

    public static final String XLSX_FORMAT = "xlsx";

    @Override
    public CsvToExcelBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                      final String targetFormat) {
        return new CsvToExcelBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return CSV_FORMAT.equalsIgnoreCase(sourceStreamer.getType()) &&
            XLSX_FORMAT.equalsIgnoreCase(targetFormat);
    }
}
