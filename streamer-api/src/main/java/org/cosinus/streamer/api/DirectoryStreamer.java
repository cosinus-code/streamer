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
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public interface DirectoryStreamer<S extends Streamer> extends Streamer<S> {

    /**
     * Get the flat stream for the stream of sub-trees given by a stream of tree roots
     *
     * @param streamerFilter the filter to apply on the direct children
     * @return the flat stream of the sub-trees
     */
    Stream<S> flatStream(StreamerFilter streamerFilter);

    /**
     * Get free space from this streamer point of view.
     *
     * @return the free space amount in bytes
     */
    long getFreeSpace();

    DirectoryStreamer createDirectoryStreamer(Path path);

    BinaryStreamer createBinaryStreamer(Path path);

    boolean rename(Path path, String newName);

    void execute(Path path);

    /**
     * Get amount of space occupied by a streamer.
     *
     * @param streamerFilter the filter to apply on the direct children
     * @return the amount of space occupied by the streamer in bytes
     */
    default long getTotalSize(StreamerFilter streamerFilter) {
        try (Stream<? extends Streamer> flatStreamers = flatStream(streamerFilter)) {
            return flatStreamers
                    .filter(not(Streamer::isDirectory))
                    .mapToLong(Streamer::getSize)
                    .sum();
        }
    }

    /**
     * Count all streamers that are not directories in the whole sub-tree.
     *
     * @param streamerFilter the filter to apply on the direct children
     * @return the number of non-directories
     */
    default long count(StreamerFilter streamerFilter) {
        try (Stream<? extends Streamer> flatStreamers = flatStream(streamerFilter)) {
            return flatStreamers
                    .filter(not(Streamer::isDirectory))
                    .count();
        }
    }

    @Override
    default boolean isDirectory() {
        return true;
    }

    default Streamer create(Path path) {
        return null;
    }
}
