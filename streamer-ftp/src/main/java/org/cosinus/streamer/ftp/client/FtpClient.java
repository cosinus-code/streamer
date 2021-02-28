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

package org.cosinus.streamer.ftp.client;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.cosinus.streamer.api.stream.FlatStreamerSpliterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Wrapper over {@link FTPClient}
 */
public class FtpClient extends FTPClient implements AutoCloseable {

    private FtpClientPool ftpClientPool;

    private final String name;

    public FtpClient(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFtpClientPool(FtpClientPool ftpClientPool) {
        this.ftpClientPool = ftpClientPool;
    }

    public Stream<FtpFile> stream(FtpFile parent) {
        return internalStream(parent)
                .onClose(this::close);
    }

    public Stream<FtpFile> flatStream(Stream<FtpFile> roots) {
        return StreamSupport
                .stream(new FlatStreamerSpliterator<>(roots, this::internalStream), false)
                .onClose(this::close);
    }

    private Stream<FtpFile> internalStream(FtpFile parent) {
        try {
            return parent.isDirectory() ?
                    Arrays.stream(listFiles(parent.getFtpPath()))
                            .map(file -> createFtpFile(parent, file)) :
                    Stream.empty();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public FtpFile createFtpFile(FtpFile parent, FTPFile file) {
        Path filePath = parent.getPath().resolve(file.getName());
        return new FtpFile(name, filePath, file);
    }

    public InputStream inputStream(FtpFile ftpFile) {
        try {
            return new FtpFileInputStream(this, ftpFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream outputStream(FtpFile ftpFile, boolean append) {
        try {
            return append ?
                    appendFileStream(ftpFile.getFtpPath()) :
                    storeFileStream(ftpFile.getFtpPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        Optional.ofNullable(ftpClientPool)
                .ifPresent(ftpClientPool -> ftpClientPool.returnObject(name, this));
    }
}
