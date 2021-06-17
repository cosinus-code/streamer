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

package org.cosinus.streamer.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Proxy for handling files.
 * {@link Files} should not be called directly, but through this handler.
 */
@Component
public class FileHandler {

    private static final Logger LOG = LogManager.getLogger(FileHandler.class);

    public Stream<Path> walk(Path path) {
        try {
            return Files.walk(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean isSameFile(Path path1, Path path2) {
        try {
            return path1.toFile().exists() &&
                path2.toFile().exists() &&
                Files.isSameFile(path1, path2);
        } catch (IOException ex) {
            LOG.error("Failed to compare files: " + path1 + " and " + path2, ex);
            return false;
        }
    }

    public Stream<String> lines(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public OutputStream outputStream(Path path, boolean append) {
        try {
            return new FileOutputStream(path.toFile(), append);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Stream<Path> roots() {
        return Arrays.stream(File.listRoots())
            .map(File::toPath);
    }

    public List<OSFileStore> getFileStores() {
        return new SystemInfo().getOperatingSystem().getFileSystem().getFileStores();
    }

    public String mimeType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
