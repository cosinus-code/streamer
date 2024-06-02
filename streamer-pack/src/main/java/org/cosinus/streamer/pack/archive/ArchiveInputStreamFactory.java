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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Component
public class ArchiveInputStreamFactory extends ArchiveStreamFactory {

    private static final Logger LOG = LogManager.getLogger(ArchiveInputStreamFactory.class);

    @Override
    public ArchiveInputStream createArchiveInputStream(InputStream inputStream) {
        try {
            return super.createArchiveInputStream(inputStream);
        } catch (ArchiveException e) {
            throw new RuntimeException("Cannot create archiver", e);
        }
    }

    @Override
    public ArchiveInputStream createArchiveInputStream(String archiverName,
                                                       InputStream inputStream) {
        try {
            return super.createArchiveInputStream(archiverName, inputStream);
        } catch (ArchiveException e) {
            throw new RuntimeException("Cannot create archiver of type: " + archiverName, e);
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
            throw new RuntimeException("Cannot create 7Z file from file: " + file.getPath(), e);
        }
    }

    public Optional<String> detectArchiverName(String filename, InputStream inputStream) {
        String extension = FilenameUtils.getExtension(filename);
        return getInputStreamArchiveNames()
            .stream()
            .filter(archiveName -> archiveName.equals(extension))
            .findFirst()
            .or(() -> filename.contains(".tar.") ? Optional.of(TAR) : empty())
            .or(() -> {
                try (InputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                    return Optional.of(detect(bufferedInputStream));
                } catch (IOException | ArchiveException e) {
                    LOG.trace(e);
                    return empty();
                }
            });
    }

    public Stream<ArchiveStreamEntry> stream(final BinaryStreamer binaryStreamer) {
        return stream(binaryStreamer, null);
    }

    public Stream<ArchiveStreamEntry> stream(final BinaryStreamer binaryStreamer, final ArchiveCache archiveCache) {
        EntryInputStream entryInputStream = createArchiveInputStream(binaryStreamer);
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

    private EntryInputStream createArchiveInputStream(final BinaryStreamer binaryStreamer) {
        return detectArchiverName(binaryStreamer.getName(), binaryStreamer.inputStream())
            .map(archiverName -> createArchiveInputStream(archiverName,
                binaryStreamer.getPath(),
                binaryStreamer.inputStream()))
            .orElseThrow(() -> new StreamerException("Cannot find a archiver for streamer: " + binaryStreamer.getPath()));
    }

    /**
     * Get the input stream corresponding to an archive entry by opening the archive the searching for the entry.
     *
     * @param binaryStreamer the archive binary streamer
     * @param archiveEntry the archive entry to open input stream for
     * @return the input stream
     */
    public InputStream inputStream(final BinaryStreamer binaryStreamer, final ArchiveStreamEntry archiveEntry) {
        EntryInputStream entryInputStream = createArchiveInputStream(binaryStreamer);
        return StreamSupport
            .stream(new ArchiveSpliterator(entryInputStream, null), false)
            .filter(archiveEntry::equals)
            .findFirst()
            .map(ArchiveStreamEntry::getArchiveEntry)
            .map(entryInputStream::getInputStream)
            .orElse(null);
    }

}
