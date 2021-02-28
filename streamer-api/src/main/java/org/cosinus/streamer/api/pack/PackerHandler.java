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

package org.cosinus.streamer.api.pack;

import org.apache.commons.lang3.ArrayUtils;
import org.cosinus.streamer.api.Streamer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * Handler for {@link MainPacker} components
 */
@Component
public class PackerHandler {

    private final Map<String, MainPacker> packers;

    public PackerHandler(List<MainPacker> streamers) {
        packers = new HashMap<>();
        streamers.forEach(
                packer -> getPackTypes(packer).forEach(type -> packers.put(type, packer)));

    }

    public boolean isPacker(Streamer streamer) {
        return streamer.getClass().getAnnotation(Packer.class) != null;
    }

    protected Stream<String> getPackTypes(MainPacker packer) {
        Packer packSystem = packer.getClass().getAnnotation(Packer.class);
        return Arrays.stream(Optional.of(packSystem.type())
                .filter(type -> !ArrayUtils.isEmpty(type))
                .orElseGet(packSystem::value));
    }

    public Optional<MainPacker> findPacker(String type) {
        return Optional.ofNullable(packers.get(type));
    }

    public Map<String, MainPacker> getPackers() {
        return packers;
    }
}
