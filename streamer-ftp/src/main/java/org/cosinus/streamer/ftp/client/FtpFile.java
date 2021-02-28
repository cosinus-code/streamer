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

package org.cosinus.streamer.ftp.client;

import org.apache.commons.net.ftp.FTPFile;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.Objects;

/**
 * Wrapper over a {@link FTPFile} which holds also tha full path
 */
public class FtpFile {

    public static final String ROOT_PATH = "./";

    private final String ftpClientName;

    private final FTPFile ftpFile;

    private final Path path;

    public FtpFile(String ftpClientName, Path path, FTPFile ftpFile) {
        this.ftpClientName = ftpClientName;
        this.ftpFile = ftpFile;
        this.path = path;
    }

    public String getFtpClientName() {
        return ftpClientName;
    }

    public String getName() {
        return ftpFile.getName();
    }

    public Path getPath() {
        return path;
    }

    public String getFtpPath() {
        return path.startsWith(ftpClientName) ?
                path.getNameCount() == 1 ?
                        ROOT_PATH :
                        path.subpath(1, path.getNameCount()).toString() :
                path.toString();
    }

    public boolean isDirectory() {
        return ftpFile.isDirectory();
    }

    public long getSize() {
        return ftpFile.getSize();
    }

    public Calendar getTimestamp() {
        return ftpFile.getTimestamp();
    }

    public boolean isSymbolicLink() {
        return ftpFile.isSymbolicLink();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FtpFile)) {
            return false;
        }

        FtpFile ftpFile = (FtpFile) other;
        return path.equals(ftpFile.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
