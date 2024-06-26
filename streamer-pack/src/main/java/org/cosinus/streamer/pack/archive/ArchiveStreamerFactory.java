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

package org.cosinus.streamer.pack.archive;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.pack.archive.stream.ArchiveCache;
import org.cosinus.streamer.pack.archive.stream.ArchiveSpliterator;
import org.cosinus.swing.util.AutoRemovableTemporaryFile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.createFile;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.cosinus.swing.util.AutoRemovableTemporaryFile.autoRemovableTemporaryFileWithExtension;
import static org.cosinus.swing.util.FileUtils.createVirtualFile;

@Component
public class ArchiveStreamerFactory extends ArchiveStreamFactory {

    private static final Logger LOG = LogManager.getLogger(ArchiveStreamerFactory.class);

    @Override
    public ArchiveInputStream createArchiveInputStream(InputStream inputStream) {
        try {
            return super.createArchiveInputStream(inputStream);
        } catch (ArchiveException e) {
            throw new StreamerException("Cannot create archiver", e);
        }
    }

    @Override
    public ArchiveInputStream createArchiveInputStream(String archiverName,
                                                       InputStream inputStream) {
        try {
            return super.createArchiveInputStream(archiverName, inputStream);
        } catch (ArchiveException e) {
            throw new StreamerException("Cannot create archiver of type: " + archiverName, e);
        }
    }

    public EntryInputStream createArchiveInputStream(String archiverName,
                                                     Path path,
                                                     InputStream inputStream) {
        if (archiverName.equals(SEVEN_Z)) {
            return ofNullable(path)
                .map(Path::toFile)
                .filter(File::exists)
                .map(this::createSevenZFile)
                .map(SevenZEntryInputStream::new)
                .orElse(null);
        }

        ArchiveInputStream archiveInputStream = createArchiveInputStream(archiverName, inputStream);
        return new ArchiveEntryInputStream(archiveInputStream);
    }

    protected SevenZFile createSevenZFile(File file) {
        try {
            return new SevenZFile(file);
        } catch (IOException e) {
            throw new StreamerException("Cannot create 7Z file from file: " + file.getPath(), e);
        }
    }

    protected SevenZOutputFile createSevenZOutputFile(File file) {
        try {
            return new SevenZOutputFile(file);
        } catch (IOException e) {
            throw new StreamerException("Cannot create 7Z file from file: " + file.getPath(), e);
        }
    }

    public String detectArchiverType(final BinaryStreamer binaryStreamer) {
        String extension = FilenameUtils.getExtension(binaryStreamer.getName());
        return getInputStreamArchiveNames()
            .stream()
            .filter(archiveName -> archiveName.equals(extension))
            .findFirst()
            .or(() -> binaryStreamer.getName().contains(".tar.") ? Optional.of(TAR) : empty())
            .or(() -> {
                try (InputStream bufferedInputStream = new BufferedInputStream(binaryStreamer.inputStream())) {
                    return Optional.of(detect(bufferedInputStream));
                } catch (IOException | ArchiveException e) {
                    LOG.trace(e);
                    return empty();
                }
            })
            .orElseThrow(() ->
                new StreamerException("Cannot find a archiver for streamer: " + binaryStreamer.getPath()));

    }

    public Stream<ArchiveStreamEntry> stream(String archiveType, final BinaryStreamer binaryStreamer) {
        return stream(archiveType, binaryStreamer, null);
    }

    public Stream<ArchiveStreamEntry> stream(String archiveType,
                                             final BinaryStreamer binaryStreamer,
                                             final ArchiveCache archiveCache) {
        EntryInputStream entryInputStream = createArchiveInputStream(archiveType, binaryStreamer);
        return StreamSupport
            .stream(new ArchiveSpliterator(entryInputStream, archiveCache), false)
            .onClose(() -> {
                if (archiveCache != null) {
                    archiveCache.setLoaded(true);
                }
                try {
                    entryInputStream.closeStream();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
    }

    public EntryInputStream createArchiveInputStream(final String archiveType,
                                                     final BinaryStreamer binaryStreamer) {
        return createArchiveInputStream(archiveType,
                binaryStreamer.getPath(),
                binaryStreamer.inputStream());
    }

    /**
     * Get the input stream corresponding to an archive entry by opening the archive the searching for the entry.
     *
     * @param binaryStreamer the archive binary streamer
     * @param archiveEntry   the archive entry to open input stream for
     * @return the input stream
     */
    public InputStream inputStream(String archiveType,
                                   final BinaryStreamer binaryStreamer,
                                   final ArchiveStreamEntry archiveEntry) {
        EntryInputStream entryInputStream = createArchiveInputStream(archiveType, binaryStreamer);
        return StreamSupport
            .stream(new ArchiveSpliterator(entryInputStream, null), false)
            .filter(archiveEntry::equals)
            .findFirst()
            .map(ArchiveStreamEntry::getArchiveEntry)
            .map(entryInputStream::getInputStream)
            .orElse(null);
    }

    @Override
    public ArchiveOutputStream createArchiveOutputStream(String archiverType, final OutputStream output) {
//        if (archiverName.equals(SEVEN_Z)) {
//            return ofNullable(path)
//                .map(Path::toFile)
//                .filter(File::exists)
//                .map(this::createSevenZOutputFile)
//                .map(SevenZEntryOutputStream::new)
//                .orElse(null);
//        }

        try {
            return super.createArchiveOutputStream(archiverType, output);
        } catch (ArchiveException e) {
            throw new StreamerException("Cannot create archive output", e);
        }
    }

    public ArchiveStreamEntry createArchiveStreamEntry(String archiveType,
                                                       Path path,
                                                       boolean parent,
                                                       long size,
                                                       final Supplier<InputStream> inputStreamSupplier) {
        try (AutoRemovableTemporaryFile tmpArchiveFile = autoRemovableTemporaryFileWithExtension(archiveType);
             ArchiveOutputStream archiveOutputStream =
                 createArchiveOutputStream(archiveType, new FileOutputStream(tmpArchiveFile.getFile()))) {
            File file = createVirtualFile(path.toString(), parent, size);
            ArchiveEntry archiveEntry = archiveOutputStream.createArchiveEntry(file, getArchiveNameForFile(file));
            return new ArchiveStreamEntry(archiveEntry, inputStreamSupplier);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getArchiveNameForFile(File file) {
        String archiveName = separatorsToUnix(file.getPath());
        return !file.isDirectory() || archiveName.endsWith("/") ? archiveName : archiveName + "/";
    }

    private ArchiveEntry createArchiveEntry(String name, boolean parent, long size) {
        return new ArchiveEntry() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public long getSize() {
                return size;
            }

            @Override
            public boolean isDirectory() {
                return parent;
            }

            @Override
            public Date getLastModifiedDate() {
                return new Date();
            }
        };
    }
}
