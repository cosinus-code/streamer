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

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.stream.consumer.DefaultStreamConsumer;
import org.cosinus.streamer.api.stream.consumer.OutputWriter;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.consumer.TemporaryFileOutputStream;
import org.cosinus.streamer.api.worker.AbstractSaveWorkerModel;
import org.cosinus.streamer.pack.archive.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.cosinus.streamer.api.stream.consumer.SuffixTemporaryFileStrategy.PART_TEMPORARY_FILE;

public class ArchiveSaveModel<A extends ArchiveStreamer<?>> extends AbstractSaveWorkerModel<OutputWriter<ArchiveOutputStream>> {

    private static final int DEFAULT_PIPELINE_RATE = 8192;

    @Autowired
    protected ArchiveInputStreamFactory archiveInputStreamFactory;

    private final ArchivePackStreamer<A> archivePackStreamer;

    private final ArchiveHolder archiveHolder;

    public ArchiveSaveModel(final ArchivePackStreamer<A> archivePackStreamer, final ArchiveHolder archiveHolder) {
        this.archivePackStreamer = archivePackStreamer;
        this.archiveHolder = archiveHolder;
    }

    @Override
    public Stream<OutputWriter<ArchiveOutputStream>> streamToSave() {
        EntryInputStream archiveInputStream = archiveInputStreamFactory
            .createArchiveInputStream(archivePackStreamer.binaryStreamer());
        return StreamSupport
            .stream(new ArchiveSaveSpliterator(archiveInputStream, archiveHolder, DEFAULT_PIPELINE_RATE), false)
            .onClose(() -> {
                try {
                    archiveInputStream.closeStream();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

    @Override
    public StreamConsumer<OutputWriter<ArchiveOutputStream>> streamConsumer() {
        return archiveInputStreamFactory
            .detectArchiverName(archivePackStreamer.getName(), archivePackStreamer.binaryStreamer().inputStream())
            .map(this::createStreamConsumer)
            .orElseThrow(() -> new StreamerException(
                "Cannot open archive output stream for: " + archivePackStreamer.getPath()));
    }

    private StreamConsumer<OutputWriter<ArchiveOutputStream>> createStreamConsumer(String archiverType) {
        try {
            File binaryFile = archivePackStreamer.getPath().toFile();
            final TemporaryFileOutputStream temporaryOutputStream =
                new TemporaryFileOutputStream(binaryFile, PART_TEMPORARY_FILE);
            ArchiveOutputStream archiveOutputStream = archiveInputStreamFactory
                .createArchiveOutputStream(archiverType, temporaryOutputStream);

            return new DefaultStreamConsumer<>(archiveOutputStream) {
                @Override
                public void afterClose(boolean failed) {
                    temporaryOutputStream.afterClose(failed);
                }
            };
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public long totalItemsToSave() {
        return archivePackStreamer.computeSize(null);
    }

    @Override
    public void setDirty(boolean dirty) {
        archiveHolder.setDirty(dirty);
    }

    @Override
    public void update(List<OutputWriter<ArchiveOutputStream>> items) {
        savedItemsCount += items
            .stream()
            .mapToLong(OutputWriter::size)
            .sum();
    }
}
