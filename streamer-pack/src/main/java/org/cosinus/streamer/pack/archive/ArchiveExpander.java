/*
 * Copyright 2025 Cosinus Software
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

package org.cosinus.streamer.pack.archive;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.expand.BinaryExpander;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.expand.Expander;

import static org.apache.commons.compress.archivers.ArchiveStreamFactory.*;
import static org.cosinus.streamer.pack.archive.ArchiveExpander.WAR;
import static org.cosinus.streamer.pack.archive.ArchiveExpander.EAR;

@Expander({TAR, ZIP, JAR, SEVEN_Z, JAR, AR, CPIO, DUMP, WAR, EAR})
public class ArchiveExpander implements BinaryExpander<ArchiveStreamer> {

    public static final String WAR = "war";
    public static final String EAR = "ear";

    @Override
    public ExpandedStreamer<ArchiveStreamer> expand(BinaryStreamer binaryStreamer) {
        return new ArchivePackStreamer<>(binaryStreamer);
    }

}
