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

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.arj.ArjArchiveEntry;
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.pack.archive.rar.RarEntryInputStream;
import org.cosinus.streamer.pack.archive.sevenz.SevenZEntryInputStream;
import org.cosinus.streamer.pack.archive.zip.ZipEntryInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.file.Files.newByteChannel;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.Optional.ofNullable;

public final class ArchiveEntryCreators {

    public static final BiFunction<String, Long, ArchiveEntry> ARJ_ARCHIVE_ENTRY_CREATOR = (archiveName, size) ->
        new ArjArchiveEntry();

    public static final BiFunction<String, Long, ArchiveEntry> ZIP_ARCHIVE_ENTRY_CREATOR = (archiveName, size) -> {
        ZipArchiveEntry archiveEntry = new ZipArchiveEntry(archiveName);
        if (size >= 0) {
            archiveEntry.setSize(size);
        }
        return archiveEntry;
    };

    public static final BiFunction<String, Long, ArchiveEntry> JAR_ARCHIVE_ENTRY_CREATOR = (archiveName, size) -> {
        JarArchiveEntry archiveEntry = new JarArchiveEntry(archiveName);
        if (size >= 0) {
            archiveEntry.setSize(size);
        }
        return archiveEntry;
    };

    public static final BiFunction<String, Long, ArchiveEntry> TAR_ARCHIVE_ENTRY_CREATOR = (archiveName, size) -> {
        TarArchiveEntry archiveEntry = new TarArchiveEntry(archiveName);
        if (size >= 0) {
            archiveEntry.setSize(size);
        }
        return archiveEntry;
    };

    public static final BiFunction<String, Long, ArchiveEntry> DUMP_ARCHIVE_ENTRY_CREATOR = (archiveName, size) -> {
        DumpArchiveEntry archiveEntry = new DumpArchiveEntry(archiveName, archiveName);
        if (size >= 0) {
            archiveEntry.setSize(size);
        }
        return archiveEntry;
    };

    public static final BiFunction<String, Long, ArchiveEntry> SEVEN_Z_ARCHIVE_ENTRY_CREATOR = (archiveName, size) -> {
        SevenZArchiveEntry archiveEntry = new SevenZArchiveEntry();
        archiveEntry.setName(archiveName);
        if (size >= 0) {
            archiveEntry.setSize(size);
        }
        return archiveEntry;
    };

    public static final Function<Path, EntryInputStream> ZIP_ARCHIVE_INPUT_STREAM_CREATOR = path ->
        ofNullable(path)
            .map(Path::toFile)
            .filter(File::exists)
            .map(file -> {
                try {
                    return ZipFile.builder()
                        .setFile(path.toFile())
                        .get();
                } catch (IOException e) {
                    throw new StreamerException("Cannot create zip from file: " + file.getPath(), e);
                }
            })
            .map(ZipEntryInputStream::new)
            .orElse(null);

    public static final Function<Path, EntryInputStream> SEVEN_Z_ARCHIVE_INPUT_STREAM_CREATOR = path ->
        ofNullable(path)
            .map(Path::toFile)
            .filter(File::exists)
            .map(file -> {
                try {
                    return new SevenZFile.Builder()
                        .setSeekableByteChannel(newByteChannel(path, READ))
                        .setDefaultName(file.getAbsolutePath())
                        .get();
                } catch (IOException e) {
                    throw new StreamerException("Cannot create 7Z file from file: " + file.getPath(), e);
                }
            })
            .map(SevenZEntryInputStream::new)
            .orElse(null);

    public static final Function<Path, EntryInputStream> TGZ_ARCHIVE_INPUT_STREAM_CREATOR = path ->
        ofNullable(path)
            .map(Path::toFile)
            .filter(File::exists)
            .map(file -> {
                try {
                    return new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
                } catch (IOException e) {
                    throw new StreamerException("Cannot create TGZ input stream from file: " + file.getPath(), e);
                }
            })
            .map(ArchiveEntryInputStream::new)
            .orElse(null);

    public static final BiFunction<String, Long, ArchiveEntry> RAR_ARCHIVE_ENTRY_CREATOR =
        //TODO
        (archiveName, size) -> null;

    public static final Function<Path, EntryInputStream> RAR_ARCHIVE_INPUT_STREAM_CREATOR = path ->
        ofNullable(path)
            .map(Path::toFile)
            .filter(File::exists)
            .map(file -> {
                try {
                    return new Archive(new FileInputStream(file));
                } catch (UnsupportedRarV5Exception ex) {
                    throw new StreamerException(ex, "Cannot handle RAR 5 format");
                } catch (RarException | IOException ex) {
                    throw new StreamerException(ex, "Cannot create RAR input stream from file: " + file.getPath());
                }
            })
            .map(RarEntryInputStream::new)
            .orElse(null);

    private ArchiveEntryCreators() {

    }
}
