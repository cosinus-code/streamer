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

import org.apache.commons.lang3.ObjectUtils;
import org.cosinus.stream.StreamSupplier;
import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.parent.StreamParentsSpliterator;
import org.cosinus.streamer.api.worker.SaveWorkerModel;
import org.cosinus.swing.security.Permissions;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.api.ParentStreamer.FOLDER_VIEW_ID;
import static org.cosinus.streamer.api.TextStreamer.TEXT_VIEW_ID;
import static org.cosinus.streamer.api.file.BaseFileStreamer.BINARY_VIEW_ID;

public interface Streamer<T> extends Streamable, StreamSupplier<T>, Comparable<Streamer<?>> {

    String DETAIL_KEY_NAME = "name";
    String DETAIL_KEY_VALUE = "value";
    String DETAIL_KEY_TYPE = "type";
    String DETAIL_KEY_SIZE = "size";
    String DETAIL_KEY_TIME = "time";
    String DETAIL_KEY_FREE_MEMORY = "detail_free_memory";
    String DETAIL_KEY_TOTAL_MEMORY = "detail_total_memory";

    Stream<T> stream();

    default String getViewId() {
        return isParent() ? FOLDER_VIEW_ID : BINARY_VIEW_ID;
    }

    default StreamConsumer<T> saver() {
        return null;
    }

    default BinaryStreamer binaryStreamer() {
        return null;
    }

    default Stream<ParentStreamer<?>> parentsStream() {
        return StreamSupport.stream(new StreamParentsSpliterator(this), false);
    }

    @Override
    ParentStreamer<?> getParent();

    default boolean isTextCompatible() {
        return false;
    }

    default boolean isImage() {
        return false;
    }

    default void reset() {
    }

    default boolean isDirty() {
        return false;
    }

    default SaveWorkerModel<?> saveModel() {
        return null;
    }

    default Permissions getPermissions() {
        return null;
    }

    default void setPermissions(Permissions permissions) {

    }

    @Override
    default boolean isParent() {
        return false;
    }

    default boolean supportsChannel() {
        return ofNullable(binaryStreamer())
            .filter(BinaryStreamer::supportsChannel)
            .isPresent();
    }

    default boolean isAncestorFor(Streamer<?> streamer) {
        if (getUrlPath() == null) {
            return true;
        }
        return ofNullable(streamer)
            .map(Streamer::getUrlPath)
            .map(path -> path.startsWith(getUrlPath()))
            .orElse(false);
    }

    @Override
    default int compareTo(Streamer<?> other) {
        if (isParent() && !other.isParent()) {
            return -1;
        }

        if (!isParent() && other.isParent()) {
            return 1;
        }

        return ObjectUtils.compare(getName(), other.getName());
    }
}
