/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.streamer.samba.connection;

import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_DIRECTORY;
import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_HIDDEN;
import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_READONLY;
import static com.hierynomus.protocol.commons.EnumWithValue.EnumUtils.isSet;
import static java.util.Optional.ofNullable;

/**
 * Remote samba file information
 *
 * @param innerPath    the path of the file relative to the connection root, using "/" as separator
 * @param directory    true if the file is a directory
 * @param size         the size in bytes
 * @param lastModified the last write time in millis
 * @param hidden       true if the file is hidden
 * @param readOnly     true if the file is read-only
 */
public record SambaFile(String innerPath, boolean directory, long size, long lastModified,
                        boolean hidden, boolean readOnly) {

    public String name() {
        return innerPath.substring(innerPath.lastIndexOf(SambaConnection.SEPARATOR) + 1);
    }

    public static SambaFile sambaFolder(String innerPath) {
        return new SambaFile(innerPath, true, 0, 0, false, false);
    }

    public static SambaFile sambaFile(String innerPath) {
        return new SambaFile(innerPath, false, 0, 0, false, false);
    }

    public static SambaFile of(String innerPath, FileIdBothDirectoryInformation info) {
        return of(innerPath,
            info.getFileAttributes(),
            info.getEndOfFile(),
            info.getLastWriteTime());
    }

    public static SambaFile of(String innerPath, FileAllInformation info) {
        return of(innerPath,
            info.getBasicInformation().getFileAttributes(),
            info.getStandardInformation().getEndOfFile(),
            info.getBasicInformation().getLastWriteTime());
    }

    private static SambaFile of(String innerPath, long attributes, long size, FileTime lastWriteTime) {
        boolean directory = isSet(attributes, FILE_ATTRIBUTE_DIRECTORY);
        return new SambaFile(
            innerPath,
            directory,
            directory ? 0 : size,
            ofNullable(lastWriteTime)
                .map(FileTime::toEpochMillis)
                .orElse(0L),
            isSet(attributes, FILE_ATTRIBUTE_HIDDEN),
            isSet(attributes, FILE_ATTRIBUTE_READONLY));
    }
}
