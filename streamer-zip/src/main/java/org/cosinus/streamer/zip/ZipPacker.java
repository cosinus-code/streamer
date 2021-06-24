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

package org.cosinus.streamer.zip;

import org.cosinus.streamer.api.InputStreamer;
import org.cosinus.streamer.api.pack.MainPacker;
import org.cosinus.streamer.api.pack.PackStreamer;
import org.cosinus.streamer.api.pack.Packer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Packer({"zip", "jar", "war", "ear"})
@ConditionalOnProperty(name = "streamer.zip.enabled", matchIfMissing = true)
public class ZipPacker implements MainPacker<ZipStreamer> {

    public static final String ZIP_PROTOCOL = "zip://";

    @Override
    public Optional<ZipStreamer> findPackedStreamer(InputStreamer mainStreamer, String path) {
        return ofNullable(mainStreamer)
            .map(this::pack)
            .flatMap(packStreamer -> packStreamer.find(path));
    }

    @Override
    public PackStreamer<ZipStreamer> pack(InputStreamer<?> streamerToPack) {
        return new ZipPackStreamer(streamerToPack);
    }
}
