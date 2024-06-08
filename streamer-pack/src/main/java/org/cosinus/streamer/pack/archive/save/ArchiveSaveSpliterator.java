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
package org.cosinus.streamer.pack.archive.save;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.cosinus.streamer.api.stream.consumer.OutputWriter;
import org.cosinus.streamer.pack.archive.EntryInputStream;
import org.cosinus.streamer.pack.archive.stream.ArchiveCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;

public class ArchiveSaveSpliterator extends AbstractSpliterator<OutputWriter<ArchiveOutputStream>> {

    private final byte[] buffer;

    private final EntryInputStream archiveInputStream;

    private final ArchiveCache archiveCache;

    private ArchiveEntry archiveEntry;

    private InputStream archiveEntryInputStream;

    public ArchiveSaveSpliterator(final EntryInputStream archiveInputStream,
                                  final ArchiveCache archiveCache,
                                  int bufferSize) {
        super(MAX_VALUE, ORDERED | NONNULL);
        this.buffer = new byte[bufferSize];
        this.archiveInputStream = archiveInputStream;
        this.archiveCache = archiveCache;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super OutputWriter<ArchiveOutputStream>> action) {
        try {
            if (archiveEntry == null) {
                archiveEntry = nextArchiveEntry();
                if (archiveEntry == null) {
                    return false;
                }
                action.accept(startArchiveEntry());
            } else if (archiveEntry.isDirectory() || readNext() <= 0) {
                action.accept(closeArchiveEntry());
                resetArchiveEntry();
            } else {
                action.accept(writeArchiveEntry(buffer));
            }

            return true;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private ArchiveEntry nextArchiveEntry() throws IOException {
        ArchiveEntry currentArchiveEntry;
        do {
            currentArchiveEntry = archiveInputStream.getNextEntry();
        } while (currentArchiveEntry != null && !archiveCache.contains(currentArchiveEntry));

        return currentArchiveEntry;
    }

    private int readNext() throws IOException {
        if (archiveEntryInputStream == null) {
            archiveEntryInputStream = archiveInputStream.getInputStream(archiveEntry);
        }
        return archiveEntryInputStream.read(buffer);
    }

    private void resetArchiveEntry() {
        archiveEntry = null;
        archiveEntryInputStream = null;
    }

    private OutputWriter<ArchiveOutputStream> startArchiveEntry() {
        return outputStream -> outputStream.putArchiveEntry(archiveEntry);
    }

    private OutputWriter<ArchiveOutputStream> writeArchiveEntry(byte[] bytes) {
        return outputStream -> outputStream.write(bytes);
    }

    private OutputWriter<ArchiveOutputStream> closeArchiveEntry() {
        return ArchiveOutputStream::closeArchiveEntry;
    }
}
