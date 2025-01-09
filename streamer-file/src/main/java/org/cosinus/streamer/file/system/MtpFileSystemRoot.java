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

import oshi.software.os.OSFileStore;

import java.io.File;
import java.nio.file.Paths;

/**
 * Implementation of {@link OSFileStore} built from the output of "gio mount -li" command line on Linux
 */
public class MtpFileSystemRoot implements OSFileStore {

    public static final String MTP_PROTOCOL = "mtp://";

    public static final String MTP_PROTOCOL_MARK = "-> " + MTP_PROTOCOL;

    public static final String MTP_MOUNT_PREFIX = "mtp:host=";

    private final String key;

    private final String name;

    private final String mount;

    private final File mountFile;

    public MtpFileSystemRoot(String key, String name, String mtpMountFolder) {
        this.key = key;
        this.name = name;
        this.mountFile = Paths.get(mtpMountFolder, MTP_MOUNT_PREFIX + key).toFile();
        this.mount = mountFile.getAbsolutePath();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVolume() {
        return "";
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public String getLogicalVolume() {
        return "";
    }

    @Override
    public String getMount() {
        return mount;
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public String getType() {
        return "MTP";
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getUUID() {
        return MTP_MOUNT_PREFIX + key;
    }

    @Override
    public long getFreeSpace() {
        return new File(mount).getFreeSpace();
    }

    @Override
    public long getUsableSpace() {
        return getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        return new File(mount).getTotalSpace();
    }

    @Override
    public long getFreeInodes() {
        return 0;
    }

    @Override
    public long getTotalInodes() {
        return 0;
    }

    @Override
    public boolean updateAttributes() {
        return false;
    }

    public boolean isMounted() {
        return true;
    }

    public boolean isInternal() {
        return false;
    }

    public boolean isValid() {
        return !key.isBlank() && mountFile.exists();
    }
}
