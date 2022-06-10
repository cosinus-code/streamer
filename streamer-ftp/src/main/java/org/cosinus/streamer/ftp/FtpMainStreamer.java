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
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.ftp.client.FtpFile;
import org.cosinus.streamer.ftp.model.FtpModel;
import org.cosinus.streamer.ftp.model.FtpModelProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.swing.image.icon.IconProvider.ICON_NETWORK;

@RootStreamer("FTP")
@ConditionalOnProperty(name = "streamer.ftp.enabled", matchIfMissing = true)
public class FtpMainStreamer extends MainStreamer<FtpStreamer> {

    public static final String FTP_PROTOCOL = "ftp://";

    private final FtpModel ftpModel;

    private final FtpHandler ftpHandler;

    public FtpMainStreamer(FtpModelProvider ftpModelProvider, FtpHandler ftpHandler) {
        this.ftpModel = ftpModelProvider.getFtpModel();
        this.ftpHandler = ftpHandler;
    }

    @Override
    public Stream<FtpStreamer> stream() {
        return ftpModel.getFtpClientNames()
                .stream()
                .map(this::createFtpConnectionStreamer);
    }

    @Override
    public Stream<FtpStreamer> flatStream(StreamerFilter streamerFilter) {
        return Stream.empty();
    }

    @Override
    public boolean isCompatible(String urlPath) {
        return false;
    }

    @Override
    public Optional<Streamer> findByUrlPath(String urlPath) {
        return Optional.empty();
    }

    @Override
    public DirectoryStreamer createDirectoryStreamer(Path path) {
        return null;
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return null;
    }

    @Override
    public boolean rename(Path path, String newName) {
        return false;
    }

    @Override
    public void execute(Path path) {

    }

    @Override
    public String getIconName() {
        return ICON_NETWORK;
    }

    private FtpStreamer createFtpStreamer(FtpFile ftpFile) {
        return createFtpStreamer(this, ftpFile);
    }

    private FtpStreamer createFtpStreamer(DirectoryStreamer parent, FtpFile ftpFile) {
        return ftpFile.isDirectory() ?
                new FtpDirectoryStreamer(ftpFile, parent, ftpHandler) :
                new FtpBinaryStreamer(ftpFile, parent, ftpHandler);
    }

    private FtpConnectionStreamer createFtpConnectionStreamer(String ftpClientName) {
        return new FtpConnectionStreamer(ftpHandler, this, ftpClientName);
    }
}
