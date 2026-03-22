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

package org.cosinus.streamer.pack.archive.sevenz;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.cosinus.streamer.pack.archive.EntryInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static java.util.Optional.ofNullable;

public class SevenZEntryInputStream implements EntryInputStream {

    private final SevenZFile sevenZFile;

    public SevenZEntryInputStream(SevenZFile sevenZFile) {
        this.sevenZFile = sevenZFile;
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return sevenZFile.getNextEntry();
    }

    @Override
    public InputStream getInputStream(ArchiveEntry archiveEntry) {
        return ofNullable(archiveEntry)
            .filter(SevenZArchiveEntry.class::isInstance)
            .map(SevenZArchiveEntry.class::cast)
            .map(this::getInputStream)
            .orElse(null);
    }

    public InputStream getInputStream(SevenZArchiveEntry archiveEntry) {
        try {
            return sevenZFile.getInputStream(archiveEntry);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void closeStream() throws IOException {
        sevenZFile.close();
    }
}
