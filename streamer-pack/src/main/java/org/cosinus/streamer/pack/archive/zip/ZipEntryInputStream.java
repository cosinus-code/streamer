/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.pack.archive.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.cosinus.streamer.pack.archive.ArchiveStreamEntry;
import org.cosinus.streamer.pack.archive.EntryInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class ZipEntryInputStream implements EntryInputStream {

    private final ZipFile zipFile;

    private final Enumeration<ZipArchiveEntry> zipEntries;

    public ZipEntryInputStream(final ZipFile zipFile) {
        this.zipFile = zipFile;
        this.zipEntries = zipFile.getEntries();
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return zipEntries.hasMoreElements() ? zipEntries.nextElement() : null;
    }

    @Override
    public InputStream getInputStream(ArchiveEntry archiveEntry) {
        return ofNullable(archiveEntry)
            .filter(ZipArchiveEntry.class::isInstance)
            .map(ZipArchiveEntry.class::cast)
            .map(this::getInputStream)
            .orElse(null);
    }

    public InputStream getInputStream(ZipArchiveEntry zipArchiveEntry) {
        try {
            return zipFile.getInputStream(zipArchiveEntry);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ArchiveEntry> findArchiveEntry(ArchiveStreamEntry archiveEntry) {
        return ofNullable(zipFile.getEntry(archiveEntry.getName()));
    }

    @Override
    public void closeStream() throws IOException {
        zipFile.close();
    }
}
