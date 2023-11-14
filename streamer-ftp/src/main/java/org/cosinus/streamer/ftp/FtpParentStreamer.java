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
import org.cosinus.streamer.api.remote.RemoteParentStreamer;
import org.cosinus.streamer.ftp.connection.FtpConnection;

import java.nio.file.Path;

public class FtpParentStreamer extends FtpStreamer<FtpStreamer<?>>
    implements RemoteParentStreamer<FtpStreamer<?>, FTPFile, FtpConnection>
{

    public FtpParentStreamer(final FTPFile ftpFile, Path path, String connectionName)
    {
        super(ftpFile, path, connectionName);
    }

    @Override
    public void execute(Path path)
    {

    }

    @Override
    public long getFreeSpace()
    {
        return 0;
    }

    @Override
    public long getTotalSpace()
    {
        return 0;
    }

    @Override
    public boolean delete()
    {
        return false;
    }

    @Override
    public FtpStreamer<?> createFromRemote(FTPFile ftpFile)
    {
        return ftpFile.isDirectory() ?
            new FtpParentStreamer(ftpFile, path.resolve(ftpFile.getName()), connectionName) :
            new FtpBinaryStreamer(ftpFile, path.resolve(ftpFile.getName()), connectionName);
    }
}
