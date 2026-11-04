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
import org.cosinus.streamer.api.transform.csv.CsvToDocBinaryStreamer;
import org.cosinus.streamer.api.transform.csv.CsvToExcelBinaryStreamer;
import org.cosinus.streamer.api.transform.txt.TxtToHtmlBinaryStreamer;
import org.cosinus.streamer.api.transform.csv.CsvToHtmlBinaryStreamer;
import org.cosinus.streamer.api.transform.csv.CsvToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToImageBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToTxtBinaryStreamer;
import org.cosinus.streamer.api.transform.excel.ExcelToCsvBinaryStreamer;
import org.cosinus.streamer.api.transform.excel.ExcelToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.html.HtmlToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.html.HtmlToDocBinaryStreamer;
import org.cosinus.streamer.api.transform.html.HtmlToImageBinaryStreamer;
import org.cosinus.streamer.api.transform.html.HtmlToTxtBinaryStreamer;
import org.cosinus.streamer.api.transform.image.ImageToImageBinaryStreamer;
import org.cosinus.streamer.api.transform.image.ImageToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfToDocBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamer;
import org.cosinus.streamer.api.transform.pdf.PdfToTxtBinaryStreamer;
import org.cosinus.streamer.api.transform.test.TransformTestApplication;
import org.cosinus.streamer.api.transform.txt.TxtToDocBinaryStreamer;
import org.cosinus.streamer.api.transform.txt.TxtToImageBinaryStreamer;
import org.cosinus.streamer.api.transform.txt.TxtToPdfBinaryStreamer;
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

    private static final List<String> FORMATS =
        List.of("csv", "xlsx", "xls", "doc", "docx", "rtf", "html", "htm", "pdf", "png", "jpg", "bmp", "txt");

    @Autowired
    private TransformBinaryStreamerHandler handler;

    @Autowired
    private List<BinaryStreamerTransformer> transformers;

    @Test
    public void testAllTransformersAreLoaded() {
        assertEquals(22, transformers.size());
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
        assertTrue(handler.transformToBinaryStreamer(file("letter.docx"), "pdf") instanceof DocToPdfBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.doc"), "pdf") instanceof DocToPdfBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.rtf"), "pdf") instanceof DocToPdfBinaryStreamer);
    }

    @Test
    public void testPdfToWord() {
        assertTrue(handler.transformToBinaryStreamer(file("report.pdf"), "docx") instanceof PdfToDocBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("report.pdf"), "doc") instanceof PdfToDocBinaryStreamer);
    }

    @Test
    public void testPdfToImage() {
        assertTrue(handler.transformToBinaryStreamer(file("report.pdf"), "png") instanceof PdfToImageBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("report.pdf"), "jpg") instanceof PdfToImageBinaryStreamer);
    }

    @Test
    public void testWordToImage() {
        assertTrue(handler.transformToBinaryStreamer(file("letter.docx"), "png") instanceof DocToImageBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.doc"), "jpg") instanceof DocToImageBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.rtf"), "png") instanceof DocToImageBinaryStreamer);
    }

    @Test
    public void testHtmlToPdf() {
        assertTrue(handler.transformToBinaryStreamer(file("page.html"), "pdf") instanceof HtmlToPdfBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("page.htm"), "pdf") instanceof HtmlToPdfBinaryStreamer);
    }

    @Test
    public void testCsvToWord() {
        assertTrue(handler.transformToBinaryStreamer(file("data.csv"), "docx") instanceof CsvToDocBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("data.csv"), "doc") instanceof CsvToDocBinaryStreamer);
    }

    @Test
    public void testCsvToHtml() {
        assertTrue(handler.transformToBinaryStreamer(file("data.csv"), "html") instanceof CsvToHtmlBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("data.csv"), "htm") instanceof CsvToHtmlBinaryStreamer);
    }

    @Test
    public void testTxtToWord() {
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "docx") instanceof TxtToDocBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "doc") instanceof TxtToDocBinaryStreamer);
    }

    @Test
    public void testTxtToPdf() {
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "pdf") instanceof TxtToPdfBinaryStreamer);
    }

    @Test
    public void testTxtToImage() {
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "png") instanceof TxtToImageBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "jpg") instanceof TxtToImageBinaryStreamer);
    }

    @Test
    public void testTxtToHtml() {
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "html") instanceof TxtToHtmlBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("notes.txt"), "htm") instanceof TxtToHtmlBinaryStreamer);
    }

    @Test
    public void testWordToTxt() {
        assertTrue(handler.transformToBinaryStreamer(file("letter.docx"), "txt") instanceof DocToTxtBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.doc"), "txt") instanceof DocToTxtBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("letter.rtf"), "txt") instanceof DocToTxtBinaryStreamer);
    }

    @Test
    public void testPdfToTxt() {
        assertTrue(handler.transformToBinaryStreamer(file("report.pdf"), "txt") instanceof PdfToTxtBinaryStreamer);
    }

    @Test
    public void testHtmlToTxt() {
        assertTrue(handler.transformToBinaryStreamer(file("page.html"), "txt") instanceof HtmlToTxtBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("page.htm"), "txt") instanceof HtmlToTxtBinaryStreamer);
    }

    @Test
    public void testHtmlToImage() {
        assertTrue(handler.transformToBinaryStreamer(file("page.html"), "png") instanceof HtmlToImageBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("page.htm"), "jpg") instanceof HtmlToImageBinaryStreamer);
    }

    @Test
    public void testHtmlToWord() {
        assertTrue(handler.transformToBinaryStreamer(file("page.html"), "docx") instanceof HtmlToDocBinaryStreamer);
        assertTrue(handler.transformToBinaryStreamer(file("page.htm"), "doc") instanceof HtmlToDocBinaryStreamer);
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
