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
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.SaveStreamerException;
import org.cosinus.streamer.ftp.client.FtpFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class FtpBinaryStreamer extends FtpStreamer<byte[]> implements BinaryStreamer {

    public FtpBinaryStreamer(FtpFile ftpFile, ParentStreamer parent, FtpHandler ftpHandler) {
        super(ftpFile, parent, ftpHandler);
    }

    @Override
    public InputStream inputStream() {
        return ftpHandler.inputStream(getFtpFile());
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return ftpHandler.outputStream(getFtpFile(), append);
    }

    @Override
    public Streamer<byte[]> save() {
        if (!ftpHandler.makeFile(getFtpFile())) {
            throw new SaveStreamerException("Failed to create file:" + getFtpFile());
        }
        return this;
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        FtpFile ftpFile = ftpHandler.createFtpFile(getFtpFile(), path, false);
        return new FtpBinaryStreamer(ftpFile, getParent(), ftpHandler);
    }

}
