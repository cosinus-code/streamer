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

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.file.system.FileSystem;
import org.cosinus.swing.io.MimeTypeResolver;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * Proxy for handling files.
 * {@link Files} should not be called directly, but through this handler.
 */
@Component
public class FileHandler {

    private static final Logger LOG = LogManager.getLogger(FileHandler.class);

    private final MimeTypeResolver mimeTypeResolver;

    private final FileSystem fileSystem;

    private List<OSFileStore> fileStores;

    public FileHandler(final MimeTypeResolver mimeTypeResolver,
                       final FileSystem fileSystem) {
        this.mimeTypeResolver = mimeTypeResolver;
        this.fileSystem = fileSystem;
    }

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
            LOG.error("Failed to compare files: {} and {}", path1, path2, ex);
            return false;
        }
    }

    public synchronized List<OSFileStore> getFileStores() {
        if (fileStores == null) {
            try {
                fileStores = new SystemInfo().getOperatingSystem().getFileSystem().getFileStores();
            } catch (Exception ex) {
                if (LOG.isDebugEnabled()) {
                    LOG.error("Failed to get file stores. Will fallback to command line.", ex);
                } else {
                    LOG.warn("Failed to get file stores. Will fallback to command line: {}", ex.getMessage());
                }
                fileStores = fileSystem.getFileSystemRoots();
            }
        }
        return fileStores;
    }

    public boolean isTextCompatible(Path path) {
        return mimeTypeResolver.isTextCompatible(path) ||
            mimeTypeResolver.hasUnknownMimeType(path);
    }

    public boolean isImage(Path path) {
        return mimeTypeResolver.isImageCompatible(path);
    }

    public void reset() {
        fileStores = null;
    }
}
