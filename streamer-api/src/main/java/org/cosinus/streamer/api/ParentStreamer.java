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

import org.cosinus.streamer.api.stream.FlatStreamingSpliterator;
import org.cosinus.streamer.api.stream.FlatStreamingStrategy;

import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.cosinus.streamer.api.StreamerFilter.ALL_STREAMERS;
import static org.cosinus.streamer.api.stream.FlatStreamingStrategy.LEVEL_UP_BOTTOM;

public interface ParentStreamer<S extends Streamer> extends Streamer<S> {

    /**
     * Get the flat stream of all sub-streamers
     *
     * @return the flat stream of sub-streamers
     */
    default Stream<S> flatStream() {
        return flatStream(LEVEL_UP_BOTTOM, ALL_STREAMERS);
    }

    /**
     * Get the flat stream of all sub-streamers
     *
     * @param streamerFilter the filter to apply on the direct children
     * @return the flat stream of sub-streamers
     */
    default Stream<S> flatStream(StreamerFilter streamerFilter) {
        return flatStream(LEVEL_UP_BOTTOM, streamerFilter);
    }

    /**
     * Get the flat stream of all sub-streamers, using a specific tree parsing strategy
     *
     * @param streamerFilter the filter to apply on the direct children
     * @return the flat stream of sub-streamers
     */
    default Stream<S> flatStream(FlatStreamingStrategy strategy, StreamerFilter streamerFilter) {
        return StreamSupport.stream(
            new FlatStreamingSpliterator(strategy, stream().filter(streamerFilter)), false);
    }

    default void execute(Path path) {

    }

    default long getFreeSpace() {
        return 0;
    }

    default long getTotalSpace() {
        return 0;
    }

    @Override
    default boolean isParent() {
        return true;
    }

    default S create(Path path, Streamer<?> source) {
        return create(path, source.isParent());
    }

    default S create(Path path, boolean parent) {
        return null;
    }
}
