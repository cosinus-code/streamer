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

import org.apache.commons.compress.archivers.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.pack.archive.stream.ArchiveCache;
import org.cosinus.streamer.pack.archive.stream.ArchiveSpliterator;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.empty;
import static java.util.stream.Stream.concat;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.cosinus.streamer.pack.archive.ArchiveExpander.RAR;
import static org.cosinus.streamer.pack.archive.ArchiveExpander.TGZ;

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

    public ArchiveType detectArchiverType(final BinaryStreamer binaryStreamer) {
        String extension = FilenameUtils.getExtension(binaryStreamer.getName());
        return concat(getInputStreamArchiveNames().stream(), Stream.of(TGZ, RAR))
            .filter(archiveName -> archiveName.equals(extension))
            .findFirst()
            .or(() -> binaryStreamer.getName().contains(".tar.") ? Optional.of(TAR) : empty())
            .or(() -> {
                try (InputStream bufferedInputStream = new BufferedInputStream(binaryStreamer.inputStream())) {
                    return Optional.of(detect(bufferedInputStream));
                } catch (IOException e) {
                    LOG.trace(e);
                    return empty();
                }
            })
            .map(ArchiveType::ofValue)
            .orElseThrow(() ->
                new StreamerException("Cannot find a archiver for streamer: " + binaryStreamer.getPath()));

    }

    public Stream<ArchiveStreamEntry> stream(ArchiveType archiveType, final BinaryStreamer binaryStreamer) {
        return stream(archiveType, binaryStreamer, null);
    }

    public Stream<ArchiveStreamEntry> stream(ArchiveType archiveType,
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

    public EntryInputStream createArchiveInputStream(final ArchiveType archiveType,
                                                     final BinaryStreamer binaryStreamer) {
        return archiveType.createEntryInputStream(binaryStreamer.getPath())
            .orElseGet(() -> createArchiveEntryInputStream(archiveType, binaryStreamer));
    }

    public EntryInputStream createArchiveEntryInputStream(final ArchiveType archiveType,
                                                          final BinaryStreamer binaryStreamer) {
        ArchiveInputStream archiveInputStream =
            createArchiveInputStream(archiveType.getName(), binaryStreamer.inputStream());
        return new ArchiveEntryInputStream(archiveInputStream);
    }

    /**
     * Get the input stream corresponding to an archive entry by opening the archive the searching for the entry.
     *
     * @param archiveType    archive type
     * @param binaryStreamer the archive binary streamer
     * @param archiveEntry   the archive entry to open input stream for
     * @return the input stream
     */
    public InputStream inputStream(final ArchiveType archiveType,
                                   final BinaryStreamer binaryStreamer,
                                   final ArchiveStreamEntry archiveEntry) {
        EntryInputStream entryInputStream = createArchiveInputStream(archiveType, binaryStreamer);
        return entryInputStream.findArchiveEntry(archiveEntry)
            .map(entryInputStream::getInputStream)
            .orElse(null);
    }

    public ArchiveOutputStream createArchiveOutputStream(ArchiveType archiverType, final OutputStream output) {
        try {
            return super.createArchiveOutputStream(archiverType.getName(), output);
        } catch (ArchiveException e) {
            throw new StreamerException("Cannot create archive output", e);
        }
    }

    public ArchiveStreamEntry createArchiveStreamEntry(ArchiveType archiveType,
                                                       Path path,
                                                       boolean parent,
                                                       long size,
                                                       final Supplier<InputStream> inputStreamSupplier) {
        String archiveName = getArchiveName(path, parent);
        ArchiveEntry archiveEntry = archiveType.createArchiveEntry(archiveName, size);
        return new ArchiveStreamEntry(archiveEntry, inputStreamSupplier);
    }

    private String getArchiveName(Path path, boolean parent) {
        String archiveName = separatorsToUnix(path.toString());
        return !parent || archiveName.endsWith("/") ? archiveName : archiveName + "/";
    }
}
