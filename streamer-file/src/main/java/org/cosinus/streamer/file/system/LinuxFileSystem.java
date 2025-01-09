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

import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.exec.ProcessExecutor;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.*;
import static org.cosinus.streamer.file.system.MtpFileSystemRoot.MTP_PROTOCOL;
import static org.cosinus.streamer.file.system.MtpFileSystemRoot.MTP_PROTOCOL_MARK;

/**
 * Implementation of {@link FileSystem} for Linux
 */
@Component
@ConditionalOnLinux
public class LinuxFileSystem implements FileSystem {

    private final ProcessExecutor processExecutor;

    public LinuxFileSystem(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    public List<OSFileStore> getFileSystemRoots() {
        List<OSFileStore> roots = new SystemInfo().getOperatingSystem().getFileSystem().getFileStores();
        roots.addAll(getMtpFilesystemRoots());
        return roots;
    }

    private List<MtpFileSystemRoot> getMtpFilesystemRoots() {
        return getMtpMountFolder()
            .map(mtpMountFolder -> getMtpMountedDevices()
                .entrySet()
                .stream()
                .map(entry -> new MtpFileSystemRoot(
                    entry.getKey().endsWith("/") ? substringBefore(entry.getKey(), "/") : entry.getKey(),
                    entry.getValue(),
                    mtpMountFolder))
                .filter(MtpFileSystemRoot::isValid)
                .toList())
            .orElseGet(Collections::emptyList);
    }

    private Map<String, String> getMtpMountedDevices() {
        return processExecutor.executePipeline(
                new String[]{"gio", "mount", "-l"}, new String[]{"grep", MTP_PROTOCOL})
            .map(output -> output.split("\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .collect(Collectors.toMap(output -> substringAfter(output, MTP_PROTOCOL_MARK).trim(),
                output -> substringBetween(output, ":", MTP_PROTOCOL_MARK).trim(),
                (key1, key2) -> key1));
    }

    private Optional<String> getMtpMountFolder() {
        return processExecutor.executePipeline(
                new String[]{"df", "-a"}, new String[]{"grep", "gvfsd-fuse"})
            .map(output -> output.split("\\s+"))
            .stream()
            .flatMap(Arrays::stream)
            .reduce((first, second) -> second);
    }

    @Override
    public boolean isHidden(OSFileStore fileStore) {
        return fileStore.getMount().startsWith("/tmp/") || fileStore.getTotalSpace() <= 0;
    }

    @Override
    public boolean isInternal(OSFileStore fileStore) {
        return ofNullable(fileStore)
            .filter(MtpFileSystemRoot.class::isInstance)
            .map(MtpFileSystemRoot.class::cast)
            .map(MtpFileSystemRoot::isInternal)
            .orElse(true);
    }
}
