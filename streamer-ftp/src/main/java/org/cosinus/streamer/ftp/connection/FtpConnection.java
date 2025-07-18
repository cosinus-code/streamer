/*
 * Copyright 2025 Cosinus Software
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
import java.util.Arrays;
import java.util.stream.Stream;

public class FtpConnection extends FTPClient implements Connection<FTPFile> {

    public static final String ROOT_PATH = "./";

    private final String name;

    public FtpConnection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public Stream<FTPFile> stream(String query) {
        try {
            return Arrays.stream(listFiles(query));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public InputStream inputStream(String query) {
        try {
            return retrieveFileStream(query);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputStream outputStream(String query, boolean append) {
        try {
            return append ?
                appendFileStream(query) :
                storeFileStream(query);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public FTPFile save(FTPFile remoteToSave) {
        try {
            //TODO
            super.makeDirectory(remoteToSave.getName());
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean delete(FTPFile remote, boolean moveToTrash) {
        return false;
    }

    @Override
    public void close() throws Exception {
        completePendingCommand();
    }
}
