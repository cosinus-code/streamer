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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.cosinus.streamer.pack.archive.stream.ArchiveSpliterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.StreamSupport;

public interface EntryInputStream {

    ArchiveEntry getNextEntry() throws IOException;

    InputStream getInputStream(ArchiveEntry archiveEntry);

    void closeStream() throws IOException;

    default Optional<ArchiveEntry> findArchiveEntry(final ArchiveStreamEntry archiveEntry) {
        return StreamSupport
            .stream(new ArchiveSpliterator(this, null), false)
            .filter(archiveEntry::equals)
            .findFirst()
            .map(ArchiveStreamEntry::getArchiveEntry);
    }
}
