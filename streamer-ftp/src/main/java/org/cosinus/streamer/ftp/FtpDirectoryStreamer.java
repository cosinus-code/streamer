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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.consumer.StreamConsumer;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.streamer.ftp.client.FtpFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public class FtpDirectoryStreamer extends FtpStreamer<FtpStreamer> implements DirectoryStreamer<FtpStreamer> {

    public FtpDirectoryStreamer(FtpFile ftpFile, DirectoryStreamer parent, FtpHandler ftpHandler) {
        super(ftpFile, parent, ftpHandler);
    }

    @Override
    public Stream<FtpStreamer> stream() {
        return ftpHandler.stream(getFtpFile())
            .map(this::createFtpStreamer);
    }

    @Override
    public Stream<FtpStreamer> flatStream(StreamerFilter streamerFilter) {
        return ftpHandler.flatStream(getFtpFile(),
                                     stream()
                                         .filter(streamerFilter)
                                         .map(FtpStreamer::getFtpFile))
            .map(this::createFtpStreamer);
    }

    @Override
    public Streamer save() {
        if (!ftpHandler.makeDirectory(getFtpFile())) {
            throw new SaveStreamerException("Failed to create directory:" + getFtpFile());
        }
        return this;
    }

    @Override
    public DirectoryStreamer createDirectoryStreamer(Path path) {
        FtpFile ftpFile = ftpHandler.createFtpFile(getFtpFile(), path, true);
        return new FtpDirectoryStreamer(ftpFile, this, ftpHandler);
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        FtpFile ftpFile = ftpHandler.createFtpFile(getFtpFile(), path, false);
        return new FtpBinaryStreamer(ftpFile, this, ftpHandler);
    }

    @Override
    public boolean rename(Path path, String newName) {
        return false;
    }

    @Override
    public void execute(Path path) {

    }

    private FtpStreamer createFtpStreamer(FtpFile ftpFile) {
        return ftpFile.isDirectory() ?
            new FtpDirectoryStreamer(ftpFile, this, ftpHandler) :
            new FtpBinaryStreamer(ftpFile, this, ftpHandler);
    }
}
