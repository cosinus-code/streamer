/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.api.expand;

import org.apache.commons.lang3.ArrayUtils;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.TextStreamer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.Arrays.stream;

/**
 * Handler for {@link BinaryExpander} components
 */
@Component
public class BinaryExpanderHandler {

    private final Map<String, BinaryExpander> binaryExpandersMap;

    public BinaryExpanderHandler(List<BinaryExpander> streamers) {
        binaryExpandersMap = new HashMap<>();
        streamers.forEach(binaryExpander -> getExpanderTypes(binaryExpander)
            .forEach(type -> binaryExpandersMap.put(type, binaryExpander)));

    }

    protected Stream<String> getExpanderTypes(BinaryExpander binaryExpander) {
        Expander expanderSystem = binaryExpander.getClass().getAnnotation(Expander.class);
        return stream(Optional.of(expanderSystem.type())
            .filter(type -> !ArrayUtils.isEmpty(type))
            .orElseGet(expanderSystem::value));
    }

    public Optional<BinaryExpander> findStreamExpander(String type) {
        return ofNullable(binaryExpandersMap.get(type));
    }

    public Map<String, BinaryExpander> getBinaryExpandersMap() {
        return binaryExpandersMap;
    }

    public Streamer expandStreamer(Streamer streamerToExpand) {
        return ofNullable(streamerToExpand)
            .map(this::checkIfStreamerIsExpandable)
            .map(this::checkIfStreamerIsText)
            .orElse(null);
    }

    private Streamer<?> checkIfStreamerIsExpandable(Streamer<?> streamerToCheck) {
        return ofNullable(streamerToCheck)
            .filter(stream -> !ExpandedStreamer.class.isAssignableFrom(stream.getClass()))
            .map(Streamer::binaryStreamer)
            .<Streamer>flatMap(binaryStream -> findStreamExpander(binaryStream.getType())
                .map(binaryExpander -> binaryExpander.expand(binaryStream)))
            .orElse(streamerToCheck);
    }

    private Streamer<?> checkIfStreamerIsText(Streamer<?> streamerToCheck) {
        return ofNullable(streamerToCheck)
            .filter(Streamer::isTextCompatible)
            .map(Streamer::binaryStreamer)
            .<Streamer<?>>map(TextStreamer::new)
            .orElse(streamerToCheck);
    }
}
