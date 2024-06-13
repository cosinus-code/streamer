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
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;
import static java.util.Optional.ofNullable;

public class ArchiveSpliterator implements Spliterator<ArchiveStreamEntry> {

    private final EntryInputStream archiveInputStream;

    private ArchiveCache archiveCache;

    public ArchiveSpliterator(final EntryInputStream archiveInputStream) {
        this.archiveInputStream = archiveInputStream;
    }

    public ArchiveSpliterator(final EntryInputStream archiveInputStream, final ArchiveCache archiveCache) {
        this.archiveInputStream = archiveInputStream;
        this.archiveCache = archiveCache;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super ArchiveStreamEntry> action) {
        Optional<ArchiveStreamEntry> entry = nextEntry();
        if (archiveCache != null && !archiveCache.isLoaded()) {
            entry.ifPresent(archiveCache::add);
        }
        entry.ifPresent(action);
        return entry.isPresent();
    }

    protected Optional<ArchiveStreamEntry> nextEntry() {
        try {
            Optional<ArchiveStreamEntry> archiveStreamEntry;
            do {
                archiveStreamEntry = ofNullable(archiveInputStream.getNextEntry())
                    .map(entry -> new ArchiveStreamEntry(entry, archiveInputStream));
            } while (archiveStreamEntry
                .filter(ArchiveStreamEntry::isOSSpecific)
                .isPresent());

            return archiveStreamEntry;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public Spliterator<ArchiveStreamEntry> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }
}
