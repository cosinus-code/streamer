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
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TransformBinaryStreamerHandler {

    private final Set<BinaryStreamerTransformer> transformersMap;

    public TransformBinaryStreamerHandler(Set<BinaryStreamerTransformer> transformers) {
        this.transformersMap = transformers;
    }

    public BinaryStreamer transformToBinaryStreamer(final Streamer<?> streamer,
                                                    final String targetFormat) {
        return transformersMap.stream()
            .filter(transformer -> transformer.canTransform(streamer, targetFormat))
            .map(transformer -> transformer.transformStreamer(streamer, targetFormat))
            .findFirst()
            .orElseGet(streamer::binaryStreamer);
    }
}
