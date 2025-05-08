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
package org.cosinus.streamer.file.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.exec.ProcessExecutor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation of {@link FileSystem} for Mac
 */
@Component
@ConditionalOnMac
public class MacFileSystem implements FileSystem {

    private static final Logger LOG = LogManager.getLogger(MacFileSystem.class);

    private final ProcessExecutor processExecutor;

    public MacFileSystem(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    public List<? extends FileSystemRoot> getFileSystemRoots() {
        try {
            return getDefaultFileSystemRoot();
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Failed to get file stores. Will fallback to command line.", ex);
            } else {
                LOG.warn("Failed to get file stores. Will fallback to command line: {}", ex.getMessage());
            }
            return getFileSystemRootsFromCommandLine();
        }
    }

    @Override
    public void mount(FileSystemRoot fileSystemRoot) {
    }

    /**
     * Get file system roots using the output of "diskutil" command
     *
     * @return A list of file system roots.
     */
    private List<MacFileSystemRoot> getFileSystemRootsFromCommandLine() {
        return processExecutor.executeAndGetOutput("diskutil", "list")
            .map(output -> output.split("\\n\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .flatMap(rawDiskData -> stream(rawDiskData.split("\\n"))
                .skip(2))
            .map(rawVolumeData -> stream(rawVolumeData.split("\\s+"))
                .reduce((first, second) -> second)
                .orElse(null))
            .filter(Objects::nonNull)
            .map(this::buildFileSystemRoot)
            .filter(root -> ((MacFileSystemRoot) root).isValid())
            .toList();
    }

    private MacFileSystemRoot buildFileSystemRoot(String volumeId) {
        return processExecutor.executeAndGetOutput("diskutil", "info", volumeId)
            .map(output -> output.split("\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .filter(not(String::isBlank))
            .map(line -> line.split(":"))
            .filter(line -> line.length > 1)
            .collect(toMap(
                property -> property[0].trim(),
                property -> property[1].trim(),
                (k1, k2) -> k1,
                MacFileSystemRoot::new));
    }
}
