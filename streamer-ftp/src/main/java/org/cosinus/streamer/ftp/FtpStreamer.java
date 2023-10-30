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
import org.cosinus.streamer.ftp.client.FtpFile;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

import static org.cosinus.streamer.ftp.FtpMainStreamer.FTP_PROTOCOL;

public abstract class FtpStreamer<T> implements Streamer<T> {

    protected final FtpHandler ftpHandler;

    private final FtpFile ftpFile;

    private final ParentStreamer parent;

    protected FtpStreamer(FtpFile ftpFile, ParentStreamer parent, FtpHandler ftpHandler) {
        this.parent = parent;
        this.ftpFile = ftpFile;
        this.ftpHandler = ftpHandler;
    }

    @Override
    public ParentStreamer getParent() {
        //TODO: To compute the parent. There is no need to keep all the parents chain
        return parent;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public String getProtocol() {
        return FTP_PROTOCOL;
    }

    @Override
    public Path getPath() {
        return ftpFile.getPath();
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public long getSize() {
        return ftpFile.getSize();
    }

    @Override
    public long lastModified() {
        return Optional.ofNullable(ftpFile.getTimestamp())
            .map(Calendar::getTimeInMillis)
            .orElse(0L);
    }

    public FtpFile getFtpFile() {
        return ftpFile;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FtpStreamer)) {
            return false;
        }

        FtpStreamer that = (FtpStreamer) other;
        return ftpFile.equals(that.ftpFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ftpFile);
    }
}
