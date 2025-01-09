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

import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.software.os.OSFileStore;

import java.util.List;

/**
 * Implementation of {@link FileSystem} for Windows
 */

@Component
@ConditionalOnWindows
public class WindowsFileSystem implements FileSystem {

    @Override
    public List<OSFileStore> getFileSystemRoots() {
        return new SystemInfo().getOperatingSystem().getFileSystem().getFileStores();
    }

    @Override
    public boolean isHidden(OSFileStore fileStore) {
        return false;
    }

    @Override
    public boolean isInternal(OSFileStore fileStore) {
        return true;
    }
}
