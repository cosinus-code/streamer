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
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.pack.archive.ArchiveEntryCreators.*;

public enum ArchiveType {
    AR(org.apache.commons.compress.archivers.ArchiveStreamFactory.AR, ArArchiveEntry::new),
    ARJ(org.apache.commons.compress.archivers.ArchiveStreamFactory.ARJ, ARJ_ARCHIVE_ENTRY_CREATOR),
    CPIO(org.apache.commons.compress.archivers.ArchiveStreamFactory.CPIO, CpioArchiveEntry::new),
    DUMP(org.apache.commons.compress.archivers.ArchiveStreamFactory.DUMP, DUMP_ARCHIVE_ENTRY_CREATOR),
    JAR(org.apache.commons.compress.archivers.ArchiveStreamFactory.JAR, JAR_ARCHIVE_ENTRY_CREATOR),
    WAR(org.cosinus.streamer.pack.archive.ArchiveExpander.WAR, JAR_ARCHIVE_ENTRY_CREATOR),
    EAR(org.cosinus.streamer.pack.archive.ArchiveExpander.EAR, JAR_ARCHIVE_ENTRY_CREATOR),
    TAR(org.apache.commons.compress.archivers.ArchiveStreamFactory.TAR, TAR_ARCHIVE_ENTRY_CREATOR),
    TGZ(org.cosinus.streamer.pack.archive.ArchiveExpander.TGZ,
        TAR_ARCHIVE_ENTRY_CREATOR,
        TGZ_ARCHIVE_INPUT_STREAM_CREATOR),
    ZIP(org.apache.commons.compress.archivers.ArchiveStreamFactory.ZIP,
        ZIP_ARCHIVE_ENTRY_CREATOR,
        ZIP_ARCHIVE_INPUT_STREAM_CREATOR),
    SEVEN_Z(org.apache.commons.compress.archivers.ArchiveStreamFactory.SEVEN_Z,
        SEVEN_Z_ARCHIVE_ENTRY_CREATOR,
        SEVEN_Z_ARCHIVE_INPUT_STREAM_CREATOR),
    RAR(org.cosinus.streamer.pack.archive.ArchiveExpander.RAR,
        RAR_ARCHIVE_ENTRY_CREATOR,
        RAR_ARCHIVE_INPUT_STREAM_CREATOR);

    private final String name;

    private final BiFunction<String, Long, ArchiveEntry> archiveEntrySupplier;

    private final Function<Path, EntryInputStream> entryInputStreamSupplier;

    ArchiveType(String name,
                BiFunction<String, Long, ArchiveEntry> archiveEntrySupplier) {
        this(name, archiveEntrySupplier, null);
    }

    ArchiveType(String name,
                BiFunction<String, Long, ArchiveEntry> archiveEntrySupplier,
                Function<Path, EntryInputStream> entryInputStreamSupplier) {
        this.name = name;
        this.archiveEntrySupplier = archiveEntrySupplier;
        this.entryInputStreamSupplier = entryInputStreamSupplier;
    }

    public static ArchiveType ofValue(String name) {
        return stream(values())
            .filter(type -> type.name.equals(name))
            .findFirst()
            .orElse(null);
    }

    public String getName() {
        return name;
    }

    public ArchiveEntry createArchiveEntry(String archiveName, long size) {
        return archiveEntrySupplier.apply(archiveName, size);
    }

    public Optional<EntryInputStream> createEntryInputStream(Path path) {
        return ofNullable(entryInputStreamSupplier)
            .map(supplier -> supplier.apply(path));
    }
}
