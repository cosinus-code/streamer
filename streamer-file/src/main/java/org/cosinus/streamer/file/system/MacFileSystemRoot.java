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

import org.apache.commons.lang3.StringUtils;
import oshi.software.os.OSFileStore;

import java.util.*;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link OSFileStore} built from the output of "diskutil info <id>" command line on Mac
 */
public class MacFileSystemRoot extends HashMap<String, String> implements OSFileStore {
    private static final String DISK_ROOT_NAME = "Volume Name";
    private static final String DISK_ROOT_MOUNT = "Mount Point";
    private static final String DISK_ROOT_TYPE = "Type (Bundle)";
    private static final String DISK_ROOT_VOLUME = "Device Node";
    private static final String DISK_ROOT_UUID = "Volume UUID";
    private static final String DISK_ROOT_TOTAL_SPACE = "Container Total Space";
    private static final String DISK_ROOT_FREE_SPACE = "Container Free Space";
    private static final String DISK_ROOT_INTERNAL = "Internal";
    private static final String DISK_ROOT_LOCATION = "Device Location:";
    private static final String DISK_ROOT_MOUNTED = "Mounted";

    private static final String YES = "Yes";

    private Long totalSpace;

    private Long freeSpace;

    private Boolean internal;

    private Boolean mounted;

    @Override
    public String getName() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getVolume() {
        return get(DISK_ROOT_VOLUME);
    }

    @Override
    public String getLabel() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getLogicalVolume() {
        return "";
    }

    @Override
    public String getMount() {
        return get(DISK_ROOT_MOUNT);
    }

    @Override
    public String getDescription() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getType() {
        return get(DISK_ROOT_TYPE);
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getUUID() {
        return get(DISK_ROOT_UUID);
    }

    @Override
    public long getFreeSpace() {
        if (freeSpace == null) {
            freeSpace = getRawLong(DISK_ROOT_FREE_SPACE);
        }
        return freeSpace;
    }

    @Override
    public long getUsableSpace() {
        return getFreeSpace();
    }

    @Override
    public long getTotalSpace() {
        if (totalSpace == null) {
            totalSpace = getRawLong(DISK_ROOT_TOTAL_SPACE);
        }
        return totalSpace;
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
        if (mounted == null) {
            mounted = YES.equals(get(DISK_ROOT_MOUNTED));
        }
        return mounted;
    }
    public boolean isInternal() {
        if (internal == null) {
            internal = DISK_ROOT_INTERNAL.equals(get(DISK_ROOT_LOCATION));
        }
        return internal;
    }

    public boolean isValid() {
        return !StringUtils.isEmpty(getMount()) && isMounted();
    }

    private long getRawLong(String propertyName) {
        return ofNullable(get(propertyName))
            .map(totalSpaceProperty -> totalSpaceProperty.split(" "))
            .map(rawFreeSpace -> rawFreeSpace[2].substring(1))
            .map(Long::parseLong)
            .orElse(0L);
    }

}
