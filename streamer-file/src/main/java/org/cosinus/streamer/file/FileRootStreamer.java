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

import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.swing.format.FormatHandler;
import org.springframework.beans.factory.annotation.Autowired;
import oshi.software.os.OSFileStore;

import java.nio.file.Paths;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_EXTERNAL;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_INTERNAL;

public class FileRootStreamer extends FileParentStreamer {

    private final OSFileStore fileStore;

    @Autowired
    private FormatHandler formatHandler;

    public FileRootStreamer(OSFileStore fileStore) {
        super(Paths.get(fileStore.getMount()));
        this.fileStore = fileStore;
    }

    @Override
    public String getName() {
        return ofNullable(super.getName())
            .filter(not(String::isEmpty))
            .filter(name -> !name.equalsIgnoreCase(fileStore.getMount()))
            .filter(name -> !name.equalsIgnoreCase(fileStore.getUUID()))
            .or(() -> ofNullable(fileStore.getName())
                .filter(not(String::isEmpty))
                .filter(name -> !name.equals(fileStore.getVolume())))
            .orElseGet(() -> formatHandler.formatShortMemorySize(fileStore.getTotalSpace()) + " Volume");
    }

    @Override
    public String getDescription() {
        return format("%s (%s %s)",
            fileStore.getDescription(),
            fileStore.getType(),
            getDevice());
    }

    private String getDevice() {
        return ofNullable(fileStore.getVolume())
            .filter(not(volume -> volume.contains(fileStore.getUUID())))
            .orElseGet(fileStore::getMount);
    }

    @Override
    public String getType() {
        return fileStore.getType();
    }

    @Override
    public boolean isHidden() {
        return fileHandler.isHidden(fileStore);
    }

    @Override
    public long getFreeSpace() {
        return fileStore.getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return fileStore.getTotalSpace();
    }

    @Override
    public FileMainStreamer getParent() {
        return fileMainStreamer;
    }

    @Override
    public String getIconName() {
        return isInternal() ? ICON_STORAGE_INTERNAL : ICON_STORAGE_EXTERNAL;
    }

    public boolean isInternal() {
        return fileHandler.isInternal(fileStore);
    }

    @Override
    public void init() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new TextValue(formatHandler.formatMemorySize(getFreeSpace())),
                new TextValue(formatHandler.formatMemorySize(getTotalSpace()))
            );
        }
    }
}
