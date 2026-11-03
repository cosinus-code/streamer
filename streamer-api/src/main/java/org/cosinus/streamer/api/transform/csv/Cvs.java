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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents the CSV content as a list of rows, where each row is a list of string values.
 * The csv content is read as UTF-8 (an optional BOM is ignored),
 * and the delimiter is detected from the first line (comma, semicolon or tab).
 * All the values are written as text cells in a single sheet.
 */
public class Cvs extends ArrayList<List<String>> {

    private static final char UTF8_BOM = '\uFEFF';

    private static final char[] CANDIDATE_DELIMITERS = {',', ';', '\t'};

    public Cvs(byte[] bytes) throws IOException {
        String cvsText = getCvsText(bytes);
        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setDelimiter(detectDelimiter(cvsText))
            .get();

        try (Reader reader = new StringReader(cvsText);
             CSVParser parser = CSVParser.parse(reader, format)) {
            parser
                .stream()
                .map(this::toValues)
                .forEach(this::add);
        }
    }

    private String getCvsText(byte[] bytes) {
        String content = new String(bytes, UTF_8);
        return !content.isEmpty() && content.charAt(0) == UTF8_BOM ?
            content.substring(1) :
            content;
    }

    private List<String> toValues(CSVRecord record) {
        return StreamSupport.stream(record.spliterator(), false).toList();
    }

    /**
     * Detect the delimiter as the candidate that appears most often in the first line.
     *
     * @param content the csv content
     * @return the detected delimiter, or comma if none of the candidates appears
     */
    private char detectDelimiter(String content) {
        int endOfLine = content.indexOf('\n');
        String firstLine = endOfLine < 0 ? content : content.substring(0, endOfLine);

        char delimiter = CANDIDATE_DELIMITERS[0];
        long maxCount = 0;
        for (char candidate : CANDIDATE_DELIMITERS) {
            long count = firstLine.chars().filter(c -> c == candidate).count();
            if (count > maxCount) {
                maxCount = count;
                delimiter = candidate;
            }
        }
        return delimiter;
    }
}
