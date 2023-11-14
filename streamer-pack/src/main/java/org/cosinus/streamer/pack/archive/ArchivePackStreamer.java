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
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.pack.PackStreamer;
import org.cosinus.streamer.pack.archive.stream.ArchiveStream;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class ArchivePackStreamer<A extends ArchiveStreamer<?>> extends PackStreamer<A> implements ParentStreamer<A>
{

    @Autowired
    private ArchiveInputStreamFactory archiveInputStreamFactory;

    private ArchiveHolder archiveHolder;

    protected ArchivePackStreamer(BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        injectContext(this);
    }

    public ArchiveHolder getArchiveHolder() {
        if (archiveHolder == null || !archiveHolder.isLoaded()) {
            archiveHolder = new ArchiveHolder();
            createStream()
                .forEach(archiveHolder::add);
            archiveHolder.setLoaded(true);
        }
        return archiveHolder;
    }

    @Override
    public Stream<A> stream() {
        if (archiveHolder == null || !archiveHolder.isLoaded()) {
            archiveHolder = new ArchiveHolder();
            return createStream()
                .peek(archiveHolder::add)
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

    protected Stream<A> flatStream(StreamerFilter streamerFilter, Path path) {
        Set<String> basePaths = ofNullable(path)
            .map(archiveHolder::listEntries)
            .orElseGet(archiveHolder::rootEntries)
            .map(this::createArchiveStreamer)
            .filter(streamerFilter)
            .map(Streamer::getPath)
            .map(Path::toString)
            .collect(Collectors.toSet());

        return createStream()
            .filter(entry -> basePaths
                .stream()
                .anyMatch(bsePath -> entry.getName().startsWith(bsePath)))
            .map(this::createArchiveStreamer);
    }

    public ArchiveBinaryStreamer createBinaryStreamer(Path path) {
        ArchiveStreamEntry archiveEntry = archiveHolder.get(path)
            .orElse(createArchiveStreamEntry(path.toString()));
        return createBinaryStreamer(archiveEntry);
    }

    protected ArchiveStreamEntry createArchiveStreamEntry(String name) {
        return new ArchiveStreamEntry(createArchiveEntry(name));
    }

    @Override
    public Optional<A> find(String path) {
        return getArchiveHolder().get(path)
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
    public void execute(Path path) {

    }

    @Override
    public long getFreeSpace() {
        return getParent().getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return getParent().getTotalSpace();
    }

    @Override
    public void finishLoading() {
        if (archiveHolder != null) {
            archiveHolder.setLoaded(true);
        }
    }

    public boolean exists(Path path) {
        return archiveHolder.get(path).isPresent();
    }

    protected Stream<ArchiveStreamEntry> createStream() {
        return ArchiveStream.stream(createArchiveInputStream(binaryStreamer));
    }

    public A createArchiveStreamer(ArchiveStreamEntry archiveEntry) {
        return archiveEntry.isDirectory() ?
            (A) createDirectoryStreamer(archiveEntry) :
            (A) createBinaryStreamer(archiveEntry);
    }

    public Stream<ArchiveStreamEntry> listEntries(Path path) {
        return archiveHolder.listEntries(path);
    }

    public ArchiveParentStreamer createDirectoryStreamer(ArchiveStreamEntry archiveEntry) {
        return new ArchiveParentStreamer(this, archiveEntry);
    }

    public ArchiveBinaryStreamer createBinaryStreamer(ArchiveStreamEntry archiveEntry) {
        return new ArchiveBinaryStreamer(this, archiveEntry);
    }

    protected ArchiveEntry createArchiveEntry(String path) {
        //TODO:
        return null;
    }

    private EntryInputStream createArchiveInputStream(BinaryStreamer streamerToPack) {
        return archiveInputStreamFactory.detectArchiverName(streamerToPack.getName(),
                                                            streamerToPack.inputStream())
            .map(archiverName -> archiveInputStreamFactory.createArchiveInputStream(archiverName,
                                                                                    streamerToPack.getPath(),
                                                                                    streamerToPack.inputStream()))
            .orElseThrow(() -> new StreamerException("Cannot find a archiver for streamer: " + streamerToPack.getPath()));
    }
}
