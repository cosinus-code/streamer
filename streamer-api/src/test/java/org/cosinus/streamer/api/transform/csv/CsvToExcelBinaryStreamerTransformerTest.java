/*
 * Copyright 2026 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.api.transform.csv;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsvToExcelBinaryStreamerTransformerTest {

    private final CsvToExcelBinaryStreamerTransformer transformer = new CsvToExcelBinaryStreamerTransformer();

    @Test
    public void testCanTransformCsvToXlsx() {
        assertTrue(transformer.canTransform(csv("data.csv", ""), "xlsx"));
        assertTrue(transformer.canTransform(csv("DATA.CSV", ""), "XLSX"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(csv("data.txt", ""), "xlsx"));
        assertFalse(transformer.canTransform(csv("data.csv", ""), "xls"));
        assertFalse(transformer.canTransform(csv("data.csv", ""), "csv"));
    }

    @Test
    public void testTransformCommaSeparatedCsv() throws IOException {
        assertEquals(List.of(
                List.of("name", "city"),
                List.of("Ana", "Cluj, RO"),
                List.of("Bob", "say \"hi\"")),
            transform("name,city\nAna,\"Cluj, RO\"\nBob,\"say \"\"hi\"\"\"\n"));
    }

    @Test
    public void testTransformDetectsSemicolonAndTabDelimiters() throws IOException {
        assertEquals(List.of(List.of("a", "b,c", "d"), List.of("1", "2", "3")),
            transform("a;b,c;d\n1;2;3\n"));
        assertEquals(List.of(List.of("a", "b;c", "d"), List.of("1", "2", "3")),
            transform("a\tb;c\td\n1\t2\t3\n"));
    }

    @Test
    public void testTransformUsesCommaWhenDelimitersAreTied() throws IOException {
        assertEquals(List.of(List.of("a;b", "c"), List.of("1;2")),
            transform("a;b,c\n1;2\n"));
    }

    @Test
    public void testTransformIgnoresBomAndKeepsDiacritics() throws IOException {
        assertEquals(List.of(List.of("nume", "oraș"), List.of("Ștefan", "Iași")),
            transform("\uFEFFnume,oraș\nȘtefan,Iași\n"));
    }

    @Test
    public void testTransformKeepsValuesAsText() throws IOException {
        assertEquals(List.of(List.of("id", "amount"), List.of("007", "12.50")),
            transform("id,amount\n007,12.50\n"));
    }

    @Test
    public void testTransformMultilineValue() throws IOException {
        assertEquals(List.of(List.of("note"), List.of("first\nsecond")),
            transform("note\n\"first\nsecond\"\n"));
    }

    @Test
    public void testTransformEmptyCsv() throws IOException {
        assertEquals(List.of(), transform(""));
    }

    private List<List<String>> transform(String csvContent) throws IOException {
        byte[] xlsx = readAll(transformer.transformStreamer(csv("data.csv", csvContent), "xlsx"));
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            assertEquals(1, workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheetAt(0);
            List<List<String>> rows = new ArrayList<>();
            for (Row row : sheet) {
                List<String> values = new ArrayList<>();
                row.forEach(cell -> values.add(cell.getStringCellValue()));
                rows.add(values);
            }
            return rows;
        }
    }

    private TestBinaryStreamer csv(String name, String content) {
        return new TestBinaryStreamer(name, content.getBytes(UTF_8));
    }
}
