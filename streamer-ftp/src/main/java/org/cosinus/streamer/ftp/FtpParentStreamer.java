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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.streamer.api.stream.FlatStreamingStrategy;
import org.cosinus.streamer.ftp.client.FtpFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public class FtpParentStreamer extends FtpStreamer<FtpStreamer> implements ParentStreamer<FtpStreamer>
{

    public FtpParentStreamer(FtpFile ftpFile, ParentStreamer parent, FtpHandler ftpHandler) {
        super(ftpFile, parent, ftpHandler);
    }

    @Override
    public Stream<FtpStreamer> stream() {
        return ftpHandler.stream(getFtpFile())
            .map(this::createFtpStreamer);
    }

    @Override
    public Stream<FtpStreamer> flatStream(FlatStreamingStrategy strategy, StreamerFilter streamerFilter) {
        return ftpHandler.flatStream(strategy, getFtpFile(),
                                     stream()
                                         .filter(streamerFilter)
                                         .map(FtpStreamer::getFtpFile))
            .map(this::createFtpStreamer);
    }

    @Override
    public void save() {
        if (!ftpHandler.makeDirectory(getFtpFile())) {
            throw new SaveStreamerException("Failed to create directory:" + getFtpFile());
        }
    }

    @Override
    public FtpStreamer create(Path path, boolean parent)
    {
        FtpFile ftpFile = ftpHandler.createFtpFile(getFtpFile(), path, parent);
        return createFtpStreamer(ftpFile);
    }

    @Override
    public void execute(Path path) {

    }

    @Override
    public long getFreeSpace() {
        //TODO
        return 0;
    }

    @Override
    public long getTotalSpace() {
        //TODO
        return 0;
    }

    private FtpStreamer createFtpStreamer(FtpFile ftpFile) {
        return ftpFile.isDirectory() ?
            new FtpParentStreamer(ftpFile, this, ftpHandler) :
            new FtpBinaryStreamer(ftpFile, this, ftpHandler);
    }
}
