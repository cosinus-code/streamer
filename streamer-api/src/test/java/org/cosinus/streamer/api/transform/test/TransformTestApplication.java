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

package org.cosinus.streamer.api.transform.test;

import org.cosinus.streamer.api.transform.TransformBinaryStreamerHandler;
import org.cosinus.streamer.api.transform.csv.CsvToDocBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.csv.CsvToExcelBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.csv.CsvToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.doc.DocToImageBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.excel.ExcelToCsvBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.excel.ExcelToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.html.HtmlToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.image.ImageToImageBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.image.ImageToPdfBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.pdf.PdfToDocBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.pdf.PdfToImageBinaryStreamerTransformer;
import org.cosinus.swing.boot.SpringSwingBootApplication;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.springframework.context.annotation.Import;

/**
 * Test application loading the transform handler with all the transformers of streamer-api.
 * <p>
 * It is in its own package, so it doesn't scan the other test classes,
 * and the beans are imported explicitly.
 */
@SpringSwingBootApplication
@Import({
    TransformBinaryStreamerHandler.class,
    CsvToExcelBinaryStreamerTransformer.class,
    ExcelToCsvBinaryStreamerTransformer.class,
    ImageToImageBinaryStreamerTransformer.class,
    ImageToPdfBinaryStreamerTransformer.class,
    ExcelToPdfBinaryStreamerTransformer.class,
    CsvToPdfBinaryStreamerTransformer.class,
    DocToPdfBinaryStreamerTransformer.class,
    DocToImageBinaryStreamerTransformer.class,
    PdfToDocBinaryStreamerTransformer.class,
    PdfToImageBinaryStreamerTransformer.class,
    HtmlToPdfBinaryStreamerTransformer.class,
    CsvToDocBinaryStreamerTransformer.class
})
public class TransformTestApplication extends SwingApplicationFrame {

    @Override
    protected void loadContent() {
    }
}
