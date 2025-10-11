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
package org.cosinus.streamer.pack.archive.save;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.cosinus.stream.consumer.OutputWriter;
import org.cosinus.streamer.pack.archive.ArchiveStreamEntry;
import org.cosinus.streamer.pack.archive.EntryInputStream;
import org.cosinus.streamer.pack.archive.stream.ArchiveCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;
import static java.util.Arrays.copyOf;

public class ArchiveSaveSpliterator extends AbstractSpliterator<OutputWriter<ArchiveOutputStream>> {

    private final byte[] buffer;

    private final EntryInputStream archiveInputStream;

    private final ArchiveCache archiveCache;

    private ArchiveEntry archiveEntry;

    private InputStream archiveEntryInputStream;

    private byte[] readBytes;

    private final Queue<ArchiveStreamEntry> additionalEntries;

    public ArchiveSaveSpliterator(final EntryInputStream archiveInputStream,
                                  final ArchiveCache archiveCache,
                                  int bufferSize) {
        super(MAX_VALUE, ORDERED | NONNULL);
        this.buffer = new byte[bufferSize];
        this.archiveInputStream = archiveInputStream;
        this.archiveCache = archiveCache;
        this.additionalEntries = new LinkedList<>(archiveCache.additionalEntries());
    }

    @Override
    public boolean tryAdvance(final Consumer<? super OutputWriter<ArchiveOutputStream>> action) {
        try {
            if (archiveEntry == null) {
                archiveEntry = nextArchiveEntry();
                if (archiveEntry == null) {
                    return false;
                }
                action.accept(new PutArchiveEntry(archiveEntry));
            } else if (!archiveEntry.isDirectory() && readNext()) {
                action.accept(new WriteArchiveEntry(getReadBytes()));
            } else {
                action.accept(new CloseArchiveEntry());
                resetArchiveEntry();
            }

            return true;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private ArchiveEntry nextArchiveEntry() throws IOException {
        ArchiveEntry nextEntry;
        do {
            nextEntry = archiveInputStream.getNextEntry();
        } while (nextEntry != null && !archiveCache.contains(nextEntry));

        if (nextEntry == null) {
            ArchiveStreamEntry archiveStreamEntry = additionalEntries.poll();
            if (archiveStreamEntry != null) {
                nextEntry = archiveStreamEntry.getArchiveEntry();
                archiveEntryInputStream = archiveStreamEntry.getEntryInputStream();
            }
        }

        return nextEntry;
    }

    private boolean readNext() throws IOException {
        if (archiveEntryInputStream == null) {
            archiveEntryInputStream = archiveInputStream.getInputStream(archiveEntry);
        }
        int readSize = archiveEntryInputStream.read(buffer);
        readBytes = readSize > 0 ? copyOf(buffer, readSize) : new byte[0];
        return readSize > 0;
    }

    private byte[] getReadBytes() {
        return readBytes;
    }

    private void resetArchiveEntry() {
        archiveEntry = null;
        archiveEntryInputStream = null;
    }
}
