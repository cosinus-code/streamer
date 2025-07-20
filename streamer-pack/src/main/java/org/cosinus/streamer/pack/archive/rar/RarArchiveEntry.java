/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.pack.archive.rar;

import com.github.junrar.rarfile.FileHeader;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.attribute.FileTime;
import java.util.Date;

import static java.util.Optional.ofNullable;

public class RarArchiveEntry implements ArchiveEntry {

    private final FileHeader fileHeader;

    private final String name;

    private final Date lastModified;

    public RarArchiveEntry(final FileHeader fileHeader) {
        this.fileHeader = fileHeader;
        this.name = ofNullable(fileHeader.getFileName())
            .map(FilenameUtils::separatorsToUnix)
            .map(name -> fileHeader.isDirectory() && !name.endsWith("/") ? name + "/" : name)
            .orElse(null);
        this.lastModified = ofNullable(fileHeader.getLastModifiedTime())
            .map(FileTime::toInstant)
            .map(Date::from)
            .orElse(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getLastModifiedDate() {
        return lastModified;
    }

    @Override
    public long getSize() {
        return fileHeader.getUnpSize();
    }

    @Override
    public boolean isDirectory() {
        return fileHeader.isDirectory();
    }

    public FileHeader getFileHeader() {
        return fileHeader;
    }
}
