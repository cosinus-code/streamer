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

package org.cosinus.streamer.pack.archive;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.util.Date;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public record VirtualDirectoryArchiveEntry(String name) implements ArchiveEntry {

    public VirtualDirectoryArchiveEntry(String name) {
        String unixName = separatorsToUnix(name);
        this.name = unixName.endsWith("/") ? unixName : unixName + "/";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public Date getLastModifiedDate() {
        return null;
    }
}
