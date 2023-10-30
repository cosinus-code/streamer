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

package org.cosinus.streamer.ftp;

import static org.apache.commons.net.ftp.FTPFile.DIRECTORY_TYPE;
import static org.apache.commons.net.ftp.FTPFile.FILE_TYPE;

import org.apache.commons.net.ftp.FTPFile;
import org.cosinus.streamer.api.stream.FlatStreamingStrategy;
import org.cosinus.streamer.ftp.client.FtpClientPool;
import org.cosinus.streamer.ftp.client.FtpFile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * FTP stuff handler.
 * This is the entry point for handling FTP clients.
 */
@Component
public class FtpHandler {

    private final FtpClientPool ftpClientPool;

    public FtpHandler(FtpClientPool ftpClientPool) {
        this.ftpClientPool = ftpClientPool;
    }

    public Stream<FtpFile> stream(FtpFile ftpFile) {
        return ftpClientPool
                .borrowObject(ftpFile.getFtpClientName())
                .stream(ftpFile);
    }

    public Stream<FtpFile> flatStream(
        FlatStreamingStrategy strategy, FtpFile parentFtpFile, Stream<FtpFile> ftpFiles) {
        return ftpClientPool
                .borrowObject(parentFtpFile.getFtpClientName())
                .flatStream(strategy, ftpFiles);
    }

    public InputStream inputStream(FtpFile ftpFile) {
        return ftpClientPool
                .borrowObject(ftpFile.getFtpClientName())
                .inputStream(ftpFile);
    }

    public OutputStream outputStream(FtpFile ftpFile, boolean append) {
        return ftpClientPool
                .borrowObject(ftpFile.getFtpClientName())
                .outputStream(ftpFile, append);
    }

    public FtpFile createFtpFile(FtpFile parent, Path path, boolean directory) {
        FTPFile file = new FTPFile();
        file.setType(directory ? DIRECTORY_TYPE : FILE_TYPE);
        file.setName(path.getFileName().toString());
        return new FtpFile(parent.getFtpClientName(), path, file);
    }

    public boolean makeDirectory(FtpFile ftpFile) {
        try {
            return ftpClientPool
                    .borrowObject(ftpFile.getFtpClientName())
                    .makeDirectory(ftpFile.getPath().toString());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean makeFile(FtpFile ftpFile) {
        try (InputStream emptyStream = new ByteArrayInputStream(new byte[0])) {
            return ftpClientPool
                    .borrowObject(ftpFile.getFtpClientName())
                    .storeFile(ftpFile.getPath().toString(), emptyStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
