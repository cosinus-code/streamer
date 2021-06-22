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

import org.cosinus.streamer.api.consumer.StreamConsumer;

import java.util.stream.Stream;

public interface Streamer<T> extends Element {

    Stream<? extends T> stream();

    StreamConsumer<? extends T> saver(boolean append);

    Streamer save();

    DirectoryStreamer getParent();

    DirectoryStreamer getRootStreamer();

    boolean delete();

    String getProtocol();

    /**
     * Get free space from this streamer point of view.
     *
     * @return the free space amount in bytes
     */
    long getFreeSpace();

    /**
     * Get total space available from this streamer point of view.
     *
     * @return the total space amount in bytes
     */
    long getTotalSpace();

    default String getUrlPath() {
        return getProtocol() + getPath();
    }

    default boolean isDirectory() {
        return false;
    }
}
