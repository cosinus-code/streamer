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

import org.apache.commons.net.ftp.FTPFile;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.remote.RemoteStreamer;
import org.cosinus.streamer.ftp.connection.FtpConnection;
import org.cosinus.streamer.ftp.connection.FtpConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ftp.FtpMainStreamer.FTP_PROTOCOL;
import static org.cosinus.streamer.ftp.connection.FtpConnection.ROOT_PATH;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class FtpStreamer<T> implements RemoteStreamer<T, FTPFile, FtpConnection> {

    @Autowired
    protected FtpConnectionPool ftpConnectionPool;

    protected final FTPFile ftpFile;

    protected final Path path;

    protected final String connectionName;

    public FtpStreamer(final FTPFile ftpFile, final Path path, String connectionName) {
        injectContext(this);
        this.ftpFile = ftpFile;
        this.path = path;
        this.connectionName = connectionName;
    }

    @Override
    public ParentStreamer<?> getParent() {
        //TODO
        return null;
    }

    @Override
    public String getProtocol() {
        return FTP_PROTOCOL;
    }

    @Override
    public Path getPath() {
        return path;
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
        return ofNullable(ftpFile.getTimestamp())
            .map(Calendar::getTimeInMillis)
            .orElse(0L);
    }

    @Override
    public boolean isLink() {
        return ftpFile.isSymbolicLink();
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return null;
    }

    @Override
    public boolean isTextCompatible() {
        return false;
    }

    @Override
    public String getStreamQuery() {
        return path.startsWith(getName()) ?
            path.getNameCount() == 1 ?
                ROOT_PATH :
                path.subpath(1, path.getNameCount()).toString() :
            path.toString();
    }

    @Override
    public String connectionName() {
        return connectionName;
    }

    @Override
    public FtpConnectionPool connectionPool() {
        return ftpConnectionPool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FtpStreamer<?> that))
            return false;
        return Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath());
    }
}
