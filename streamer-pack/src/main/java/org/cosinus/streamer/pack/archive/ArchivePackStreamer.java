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

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.streamer.pack.archive.save.ArchiveSaveModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class ArchivePackStreamer<A extends ArchiveStreamer<?>> extends ExpandedStreamer<A> implements ParentStreamer<A> {

    @Autowired
    protected ArchiveStreamerFactory archiveStreamerFactory;

    private final ArchiveHolder archiveHolder;

    private ArchiveType archiveType;

    private ArchiveOutputStream archiveOutputStream;

    protected ArchivePackStreamer(BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        injectContext(this);
        archiveHolder = new ArchiveHolder();
    }

    @Override
    public Stream<A> stream() {
        if (!archiveHolder.isLoaded()) {
            return archiveStreamerFactory.stream(getArchiveType(), binaryStreamer, archiveHolder)
                .filter(entry -> entry.getParentPath().isEmpty())
                .map(this::createArchiveStreamer);
        }

        return archiveHolder.rootEntries()
            .map(this::createArchiveStreamer);
    }

    @Override
    public Stream<A> flatStream(StreamerFilter streamerFilter) {
        return flatStream(streamerFilter, null);
    }

    protected Stream<A> flatStream(StreamerFilter streamerFilter, Path parentPath) {
        Set<String> basePaths = getBasePaths(streamerFilter, parentPath);
        return archiveStreamerFactory.stream(getArchiveType(), binaryStreamer)
            .filter(entry -> basePaths
                .stream()
                .anyMatch(bsePath -> entry.getName().startsWith(bsePath)))
            .map(this::createArchiveStreamer);
    }

    @Override
    public A create(Path path, boolean parent) {
        ArchiveStreamEntry archiveEntry = archiveHolder.get(path)
            .orElse(createArchiveStreamEntry(path, parent, 0L, null));
        return createArchiveStreamer(archiveEntry);
    }

    @Override
    public A create(Path path, Streamer<?> source) {
        ArchiveStreamEntry archiveEntry = archiveHolder.get(path)
            .orElse(createArchiveStreamEntry(path, source.isParent(), source.getSize(),
                () -> source instanceof BinaryStreamer binaryStreamer ? binaryStreamer.inputStream() : null));
        return createArchiveStreamer(archiveEntry);
    }

    protected ArchiveStreamEntry createArchiveStreamEntry(Path path, boolean parent, long size,
                                                          final Supplier<InputStream> inputStreamSupplier) {
        ArchiveStreamEntry archiveStreamEntry =
            archiveStreamerFactory.createArchiveStreamEntry(getArchiveType(), path, parent, size, inputStreamSupplier);
        archiveHolder.addAdditional(archiveStreamEntry);
        return archiveStreamEntry;
    }

    @Override
    public Optional<A> find(String path) {
        if (!archiveHolder.isLoaded()) {
            try (Stream<ArchiveStreamEntry> input =
                     archiveStreamerFactory.stream(getArchiveType(), binaryStreamer, archiveHolder)) {
                input.toList();
            }
        }
        return archiveHolder.get(path)
            .map(this::createArchiveStreamer);
    }

    public Optional<ParentStreamer> findDirectoryStreamer(Path path) {
        return ofNullable(path)
            .map(Path::toString)
            .flatMap(this::find)
            .filter(streamer -> ParentStreamer.class.isAssignableFrom(streamer.getClass()))
            .map(ParentStreamer.class::cast);
    }

    @Override
    public long getFreeSpace() {
        return getParent().getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return getParent().getTotalSpace();
    }

    public boolean exists(Path path) {
        return archiveHolder.get(path).isPresent() &&
            archiveHolder.getAdditional(path).isEmpty();
    }

    public A createArchiveStreamer(ArchiveStreamEntry archiveEntry) {
        return archiveEntry.isDirectory() ?
            (A) createParentStreamer(archiveEntry) :
            (A) createBinaryStreamer(archiveEntry);
    }

    public Stream<ArchiveStreamEntry> listEntries(Path path) {
        return archiveHolder.listEntries(path);
    }

    public ArchiveParentStreamer createParentStreamer(ArchiveStreamEntry archiveEntry) {
        return new ArchiveParentStreamer(this, archiveEntry);
    }

    public ArchiveBinaryStreamer createBinaryStreamer(ArchiveStreamEntry archiveEntry) {
        return new ArchiveBinaryStreamer(this, archiveEntry);
    }

    @Override
    public long getSize() {
        return -1;
    }

    @Override
    public void reset() {
        archiveHolder.evict();
    }

    public boolean delete(ArchiveStreamEntry archiveEntry) {
        return archiveHolder.evict(archiveEntry);
    }

    @Override
    public boolean isDirty() {
        return archiveHolder.isDirty();
    }

    @Override
    public SaveWorkerModel<?> saveModel() {
        return new ArchiveSaveModel(this, archiveHolder);
    }

    public long computeSize() {
        //TODO: to much "null"
        return computeSize(null, null);
    }

    public long computeSize(final StreamerFilter streamerFilter, Path parentPath) {
        Set<String> basePaths = getBasePaths(streamerFilter, parentPath);
        return archiveHolder.listEntries()
            .filter(not(ArchiveStreamEntry::isDirectory))
            .filter(entry -> basePaths.isEmpty() || basePaths
                .stream()
                .anyMatch(path -> entry.getName().startsWith(path)))
            .mapToLong(ArchiveStreamEntry::getSize)
            .sum();
    }

    private Set<String> getBasePaths(final StreamerFilter streamerFilter, Path parentPath) {
        if (streamerFilter == null) {
            return emptySet();
        }
        return ofNullable(parentPath)
            .map(archiveHolder::listEntries)
            .orElseGet(archiveHolder::rootEntries)
            .map(this::createArchiveStreamer)
            .filter(streamerFilter)
            .map(Streamer::getPath)
            .map(Path::toString)
            .collect(Collectors.toSet());
    }

    public ArchiveType getArchiveType() {
        if (archiveType == null) {
            archiveType = archiveStreamerFactory.detectArchiverType(binaryStreamer);
        }
        return archiveType;
    }

    @Override
    public OutputStream outputStream(boolean append) {
        archiveOutputStream = archiveStreamerFactory
            .createArchiveOutputStream(getArchiveType(), binaryStreamer.outputStream(append));
        return archiveOutputStream;
    }

    public void save(ArchiveParentStreamer archiveParentStreamer) {
        try {
            archiveOutputStream.putArchiveEntry(archiveParentStreamer.getArchiveEntry());
            archiveOutputStream.closeArchiveEntry();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream entryOutputStream(ArchiveBinaryStreamer archiveBinaryStreamer) {
        if (archiveOutputStream == null) {
            return null;
        }

        try {
            archiveOutputStream.putArchiveEntry(archiveBinaryStreamer.getArchiveEntry());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new FilterOutputStream(archiveOutputStream) {
            @Override
            public void close() {
                try {
                    archiveOutputStream.closeArchiveEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}
