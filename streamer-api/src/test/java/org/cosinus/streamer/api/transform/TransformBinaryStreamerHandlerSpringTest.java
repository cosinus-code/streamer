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

package org.cosinus.streamer.api.transform;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.csv.CsvToExcelBinaryStreamer;
import org.cosinus.streamer.api.transform.csv.ExcelToCsvBinaryStreamer;
import org.cosinus.streamer.api.transform.image.ImageToImageBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.CsvToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.ExcelToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.ImageToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.WordToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.test.TransformTestApplication;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test the transform handler with the real transformers, as wired by spring.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TransformTestApplication.class)
public class TransformBinaryStreamerHandlerSpringTest {

    private static final List<String> FORMATS = List.of("csv", "xlsx", "xls", "pdf", "png", "jpg", "bmp", "txt");

    @Autowired
    private TransformBinaryStreamerHandler handler;

    @Autowired
    private List<BinaryStreamerTransformer> transformers;

    @Test
    public void testAllTransformersAreLoaded() {
        assertEquals(7, transformers.size());
    }

    @Test
    public void testCsvToXlsx() {
        assertTrue(handler.transformToBinaryStreamer(file("data.csv"), "xlsx") instanceof CsvToExcelBinaryStreamer);
    }

    @Test
    public void testExcelToCsv() {
        assertTrue(handler.transformToBinaryStreamer(file("data.xlsx"), "csv") instanceof ExcelToCsvBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("data.xls"), "csv") instanceof ExcelToCsvBinaryStreamer);
    }

    @Test
    public void testImageToImage() {
        assertTrue(handler.transformToBinaryStreamer(image("photo.png", new byte[0]), "jpg")
            instanceof ImageToImageBinaryStreamer);
    }

    @Test
    public void testImageToPdf() {
        assertTrue(handler.transformToBinaryStreamer(image("photo.png", new byte[0]), "pdf")
            instanceof ImageToPdfBinaryStreamer);
    }

    @Test
    public void testExcelToPdf() {
        assertTrue(handler.transformToBinaryStreamer(file("data.xlsx"), "pdf") instanceof ExcelToPdfBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("data.xls"), "pdf") instanceof ExcelToPdfBinaryStreamer);
    }

    @Test
    public void testCsvToPdf() {
        assertTrue(handler.transformToBinaryStreamer(file("data.csv"), "pdf") instanceof CsvToPdfBinaryStreamer);
    }

    @Test
    public void testWordToPdf() {
        assertTrue(handler.transformToBinaryStreamer(file("letter.docx"), "pdf") instanceof WordToPdfBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.doc"), "pdf") instanceof WordToPdfBinaryStreamer);
    }

    @Test
    public void testWithoutTransformationReturnsTheSourceBinaryStreamer() {
        BinaryStreamer csv = file("data.csv");
        BinaryStreamer png = image("photo.png", new byte[0]);
        BinaryStreamer notImage = file("photo.png");

        assertSame(csv, handler.transformToBinaryStreamer(csv, "csv"));
        assertSame(png, handler.transformToBinaryStreamer(png, "png"));
        assertSame(png, handler.transformToBinaryStreamer(png, "xlsx"));
        assertSame(notImage, handler.transformToBinaryStreamer(notImage, "jpg"));
    }

    /**
     * The transformers are kept in a set, so the chosen one would be random
     * if several of them could transform the same source to the same target format.
     */
    @Test
    public void testAtMostOneTransformerMatchesEachTransformation() {
        for (String sourceFormat : FORMATS) {
            for (String targetFormat : FORMATS) {
                for (TestBinaryStreamer source : List.of(
                    file("source." + sourceFormat),
                    image("source." + sourceFormat, new byte[0]))) {

                    List<BinaryStreamerTransformer> matching = transformers.stream()
                        .filter(transformer -> transformer.canTransform(source, targetFormat))
                        .toList();
                    assertTrue("%s (image: %s) -> %s matched by %s".formatted(
                            sourceFormat, source.isImage(), targetFormat, matching),
                        matching.size() <= 1);
                }
            }
        }
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
