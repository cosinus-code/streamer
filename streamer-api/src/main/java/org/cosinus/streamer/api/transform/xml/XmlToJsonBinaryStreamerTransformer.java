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

package org.cosinus.streamer.api.transform.xml;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.transform.BinaryStreamerTransformer;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.cosinus.streamer.api.transform.json.JsonFormat.JSON_FORMAT;
import static org.cosinus.streamer.api.transform.xml.XmlFormat.XML_FORMAT;

/**
 * {@link BinaryStreamerTransformer} from xml to json.
 */
@Component
public class XmlToJsonBinaryStreamerTransformer implements BinaryStreamerTransformer {

    @Override
    public XmlToJsonBinaryStreamer transformStreamer(final Streamer<?> sourceStreamer,
                                                     final String targetFormat) {
        return new XmlToJsonBinaryStreamer(sourceStreamer.binaryStreamer());
    }

    @Override
    public List<String> sourceFormats() {
        return List.of(XML_FORMAT);
    }

    @Override
    public List<String> targetFormats() {
        return List.of(JSON_FORMAT);
    }

    @Override
    public boolean canTransform(final Streamer<?> sourceStreamer, final String targetFormat) {
        return !sourceStreamer.isImage() &&
            XML_FORMAT.equalsIgnoreCase(sourceStreamer.getType()) &&
            JSON_FORMAT.equalsIgnoreCase(targetFormat);
    }
}
