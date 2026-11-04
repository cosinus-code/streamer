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

package org.cosinus.streamer.api.transform;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;

import java.util.List;

public interface BinaryStreamerTransformer {

    /**
     * @return the formats (file types, in lower case) of the sources this transformer can transform
     */
    List<String> sourceFormats();

    /**
     * @return the formats (file types, in lower case) this transformer can transform to
     */
    List<String> targetFormats();

    BinaryStreamer transformStreamer(final Streamer<?> sourceStreamer, String targetFormat);

    boolean canTransform(Streamer<?> sourceStreamer, String targetFormat);
}
