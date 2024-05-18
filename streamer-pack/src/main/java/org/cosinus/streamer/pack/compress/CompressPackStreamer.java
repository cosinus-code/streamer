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

package org.cosinus.streamer.pack.compress;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class CompressPackStreamer extends ExpandedStreamer<CompressStreamer> implements ParentStreamer<CompressStreamer> {

    private static final Logger LOG = LogManager.getLogger(CompressPackStreamer.class);

    @Autowired
    private CompressorInputStreamFactory compressorInputStreamFactory;

    public CompressPackStreamer(BinaryStreamer packBinaryStreamer) {
        super(packBinaryStreamer);
        injectContext(this);
    }

    @Override
    public Stream<CompressStreamer> stream() {
        return Stream.of(createCompressStreamer());
    }

    @Override
    public Stream<CompressStreamer> flatStream(StreamerFilter streamerFilter) {
        return stream();
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
    public Optional<CompressStreamer> find(String path) {
        return Optional.of(createCompressStreamer());
    }

    @Override
    public void finishLoading() {

    }

    protected String getDefaultCompressedStreamerName() {
        return removeExtension(getName());
    }

    protected CompressStreamer createCompressStreamer() {
        CompressorInputStream compressorInputStream = createCompressorInputStream();
        CompressMetadata metadata = compressorInputStreamFactory.createMetadata(compressorInputStream);
        if (metadata.getName() == null) {
            metadata.setName(getDefaultCompressedStreamerName());
        }
        if (metadata.getModificationTime() <= 0) {
            metadata.setModificationTime(lastModified());
        }
        if (metadata.getSize() <= 0) {
            findCompressedFileSize()
                .filter(size -> size >= getSize())
                .ifPresent(metadata::setSize);
        }
        return new CompressStreamer(this, metadata, compressorInputStream);
    }

    protected CompressorInputStream createCompressorInputStream() {
        return compressorInputStreamFactory.detectCompressorName(binaryStreamer.getType(),
                binaryStreamer.inputStream())
            .map(compressorName -> compressorInputStreamFactory.createCompressorInputStream(compressorName,
                binaryStreamer.inputStream()))
            .orElseThrow(() -> new StreamerException("Cannot find compressor for streamer of type: " + binaryStreamer.getType()));
    }

    protected Optional<Long> findCompressedFileSize() {
        File file = getPath().toFile();
        if (file.exists()) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                randomAccessFile.seek(randomAccessFile.length() - 4);
                int b4 = randomAccessFile.read();
                int b3 = randomAccessFile.read();
                int b2 = randomAccessFile.read();
                int b1 = randomAccessFile.read();
                return Optional.of((long) ((b1 << 24) | (b2 << 16) + (b3 << 8) + b4));
            } catch (IOException e) {
                LOG.error("Failed to read compressed file size", e);
            }
        }
        return Optional.empty();
    }
}
