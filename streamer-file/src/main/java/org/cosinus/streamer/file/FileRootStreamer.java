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

package org.cosinus.streamer.file;

import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.file.system.FileSystemDevice;
import org.cosinus.streamer.file.system.FileSystemRoot;
import org.cosinus.swing.format.FormatHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_INTERNAL;

public class FileRootStreamer extends FileParentStreamer {

    protected static final int DETAIL_INDEX_FREE_SPACE = 2;
    protected static final int DETAIL_INDEX_TOTAL_SPACE = 3;

    private final FileSystemRoot fileSystemRoot;

    @Autowired
    protected FormatHandler formatHandler;

    public FileRootStreamer(final FileSystemRoot fileSystemRoot) {
        super(ofNullable(fileSystemRoot.getMountPoint())
            .map(Paths::get)
            .orElse(null));
        this.fileSystemRoot = fileSystemRoot;
    }

    @Override
    public Stream<FileStreamer<?>> stream() {
        if (!fileSystemRoot.isMounted()) {
            fileHandler.mount(fileSystemRoot);
            this.file = ofNullable(fileSystemRoot.getMountPoint())
                .map(File::new)
                .orElse(null);
        }
        return super.stream();
    }

    @Override
    public String getName() {
        return ofNullable(fileSystemRoot.getLabel())
            .filter(not(String::isEmpty))
            .or(() -> ofNullable(super.getName())
                .filter(not(String::isEmpty))
                .filter(name -> !name.equalsIgnoreCase(fileSystemRoot.getMountPoint()))
                .filter(name -> !name.equalsIgnoreCase(fileSystemRoot.getUuid())))
            .or(() -> ofNullable(fileSystemRoot.getName())
                .filter(not(String::isEmpty))
                .filter(name -> !name.equals(fileSystemRoot.getVolume())))
            .orElseGet(() -> formatHandler.formatShortMemorySize(fileSystemRoot.getTotalSpace()) + " Volume");
    }

    @Override
    public String getDescription() {
        return format("%s (%s %s), %s/%s",
            fileSystemRoot.getDescription(),
            fileSystemRoot.getType(),
            getDevice(),
            ofNullable(details().get(DETAIL_INDEX_FREE_SPACE))
                .map(Objects::toString)
                .orElse("-"),
            ofNullable(details().get(DETAIL_INDEX_TOTAL_SPACE))
                .map(Objects::toString)
                .orElse("-"));
    }

    private String getDevice() {
        return ofNullable(fileSystemRoot.getVolume())
            .filter(not(volume -> volume.contains(fileSystemRoot.getUuid())))
            .orElseGet(fileSystemRoot::getMountPoint);
    }

    @Override
    public String getType() {
        return fileSystemRoot.getType();
    }

    @Override
    public boolean isHidden() {
        return fileSystemRoot.isHidden();
    }

    @Override
    public long getFreeSpace() {
        return fileSystemRoot.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return fileSystemRoot.getTotalSpace();
    }

    @Override
    public FileMainStreamer getParent() {
        return fileMainStreamer;
    }

    @Override
    public String getIconName() {
        return ofNullable(fileSystemRoot.getDevice())
            .map(FileSystemDevice::getIconName)
            .orElse(ICON_STORAGE_INTERNAL);
    }

    @Override
    public List<Value> details() {
        init();
        return details;
    }

    @Override
    public void init() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new TextValue(getType()),
                new TextValue(formatHandler.formatMemorySize(getFreeSpace())),
                new TextValue(formatHandler.formatMemorySize(getTotalSpace()))
            );
        }
    }

    @Override
    public boolean delete(boolean moveToTrash) {
        return false;
    }
}
