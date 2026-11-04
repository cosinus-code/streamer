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

package org.cosinus.streamer.api.transform.html;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocToPdfBinaryStreamer;
import org.cosinus.streamer.api.transform.doc.DocxElement;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * {@link TransformBinaryStreamer} that converts html content into pdf content.
 * <p>
 * The html is read as paragraphs and tables (see {@link HtmlWordReader}),
 * and written in pdf as the word documents are (see {@link DocToPdfBinaryStreamer}).
 */
public class HtmlToPdfBinaryStreamer extends DocToPdfBinaryStreamer {

    public HtmlToPdfBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected List<DocxElement> read(final InputStream input) throws IOException {
        return HtmlWordReader.read(input);
    }
}
