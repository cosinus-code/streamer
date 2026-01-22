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
package org.cosinus.streamer.api;

import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;

import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.cosinus.streamer.api.Streamer.DETAIL_KEY_NAME;
import static org.cosinus.streamer.api.value.TranslatableName.translatableNames;

public interface Streamable {

    Path getPath();

    default String getProtocol() {
        return ofNullable(getParent())
            .map(Streamable::getProtocol)
            .orElse(null);
    }

    default String getId() {
        return ofNullable(getPath())
            .map(Path::toString)
            .map(path -> ofNullable(getProtocol())
                .map(protocol -> protocol.concat(path))
                .orElse(path))
            .orElseGet(() -> ofNullable(getName())
                .map(name -> ofNullable(getProtocol())
                    .map(protocol -> protocol.concat(name))
                    .orElse(name))
                .orElse(""));
    }

    default String getName() {
        return ofNullable(getPath())
            .map(Path::getFileName)
            .map(Path::toString)
            .orElseGet(() -> ofNullable(getPath())
                .map(Object::toString)
                .orElse(""));
    }

    default String getUrlPath() {
        String pathText = ofNullable(getPath())
            .map(Path::toString)
            .orElse("");
        return ofNullable(getProtocol())
            .map(protocol -> protocol.concat(pathText))
            .orElse(pathText);
    }

    default String getDescription() {
        return null;
    }

    default String getType() {
        return getExtension(getName());
    }

    default boolean isParent() {
        return false;
    }

    default long getSize() {
        return -1;
    }

    default long lastModified() {
        return 0;
    }

    default boolean isHidden() {
        return false;
    }

    default boolean isLink() {
        return getLinkedStreamer() != null;
    }

    default Streamer<?> getLinkedStreamer() {
        return null;
    }

    default boolean exists() {
        return true;
    }

    default void save() {
    }

    default boolean rename(Path newPath) {
        return false;
    }

    default boolean delete(boolean moveToTrash) {
        return false;
    }

    default String getIconName() {
        return null;
    }

    default boolean canRead() {
        return true;
    }

    default boolean canUpdate() {
        return true;
    }

    default List<TranslatableName> detailNames() {
        return translatableNames(DETAIL_KEY_NAME);
    }

    default List<Value> details() {
        return singletonList(new TextValue(getName()));
    }

    default void init() {
    }

    Streamable getParent();


    default void updateDetail(int detailIndex, Object value) {
        details().get(detailIndex).setValue(value);
    }

    default boolean canUpdateDetail(int detailIndex) {
        return detailIndex == 0;
    }

    default int getLeadDetailIndex() {
        return 0;
    }

    default boolean isFile() {
        return false;
    }
}
