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

import org.cosinus.streamer.api.DirectoryStreamer;
import oshi.software.os.OSFileStore;

import java.nio.file.Paths;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_INTERNAL;
import static org.cosinus.swing.util.Formatter.formatShortMemorySize;

public class FileRootStreamer extends FileDirectoryStreamer {

    private final OSFileStore fileStore;

    public FileRootStreamer(FileMainStreamer fileMainStreamer,
                            FileHandler fileHandler,
                            OSFileStore fileStore) {
        super(fileMainStreamer, fileHandler, Paths.get(fileStore.getMount()));
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
            .orElseGet(() -> formatShortMemorySize(fileStore.getTotalSpace()) + " Volume");
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
    public DirectoryStreamer getParent() {
        return fileMainStreamer;
    }

    @Override
    public String getIconName() {
        return ICON_STORAGE_INTERNAL;
    }
}
