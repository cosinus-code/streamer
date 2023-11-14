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
package org.cosinus.streamer.ftp.connection;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.cosinus.streamer.api.remote.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class FtpConnection extends FTPClient implements Connection<FTPFile> {

    public static final String ROOT_PATH = "./";

    private final String name;

    public FtpConnection(String name) {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public Stream<FTPFile> stream(Path path)
    {
        try
        {
            return Arrays.stream(listFiles(ftpPath(path)));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream inputStream(Path path)
    {
        try
        {
            return retrieveFileStream(ftpPath(path));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputStream outputStream(Path path, boolean append)
    {
        try {
            return append ?
                appendFileStream(ftpPath(path)) :
                storeFileStream(ftpPath(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean makeDirectory(Path path)
    {
        try
        {
            return makeDirectory(ftpPath(path));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws Exception
    {
        completePendingCommand();
    }

    public String ftpPath(Path path) {
        return path.startsWith(getName()) ?
            path.getNameCount() == 1 ?
                ROOT_PATH :
                path.subpath(1, path.getNameCount()).toString() :
            path.toString();
    }
}
