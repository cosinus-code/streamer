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

package org.cosinus.streamer.pack.archive.stream;

import org.cosinus.streamer.pack.archive.ArchiveStreamEntry;
import org.cosinus.streamer.pack.archive.EntryInputStream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ArchiveStream {

    public static Stream<ArchiveStreamEntry> stream(EntryInputStream archiveInputStream) {
        return StreamSupport
            .stream(new ArchiveSpliterator(archiveInputStream), false)
            .onClose(() -> close(archiveInputStream));
    }

    protected static void close(EntryInputStream archiveInputStream) {
        try {
            archiveInputStream.closeStream();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
