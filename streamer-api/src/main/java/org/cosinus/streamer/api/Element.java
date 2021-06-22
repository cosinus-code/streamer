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

package org.cosinus.streamer.api;

import java.nio.file.Path;

import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.cosinus.swing.util.Formatter.formatMemorySize;

public interface Element {

    Path getPath();

    boolean isDirectory();

    boolean exists();

    long getSize();

    long lastModified();

    default boolean isPack() {
        return false;
    }

    default boolean canRead() {
        return true;
    }

    default boolean canWrite() {
        return true;
    }

    //TODO: to find a better name
    default boolean isSensitiveToTransferType() {
        return false;
    }

    default String getName() {
        return ofNullable(getPath().getFileName())
            .map(Path::toString)
            .orElseGet(() -> getPath().toString());
    }

    default String getType() {
        return getExtension(getName());
    }

    default boolean isLink() {
        return false;
    }

    default boolean isHidden() {
        return false;
    }

    default String getIconName() {
        return null;
    }

    default String getValue() {
        return null;
    }

    default String getDescription() {
        return null;
    }

    default String getFormattedSize() {
        return isDirectory() ? "" : formatMemorySize(getSize());
    }
}
