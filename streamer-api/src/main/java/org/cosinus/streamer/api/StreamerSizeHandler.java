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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class StreamerSizeHandler {

    private static final String STREAMER_SIZES_CACHE_NAME = "streamer.sizes";

    private static final String STREAMER_SIZE_KEY = "#streamer.getId";

    private final Set<String> computingStreamerSize;

    public StreamerSizeHandler() {
        this.computingStreamerSize = new HashSet<>();
    }

    @Cacheable(value = STREAMER_SIZES_CACHE_NAME, key = STREAMER_SIZE_KEY)
    public long getSize(Streamer streamer) {
        return -1;
    }

    @CachePut(value = STREAMER_SIZES_CACHE_NAME, key = STREAMER_SIZE_KEY)
    public long cacheStreamerSize(Streamer streamer, long size) {
        return size;
    }

    @CacheEvict(value = STREAMER_SIZES_CACHE_NAME, allEntries = true)
    public void resetAllSizes() {
    }

    public void startComputingStreamerSize(Streamer<?> streamer) {
        computingStreamerSize.add(streamer.getId());
    }

    public boolean isStreamerSizeComputing(Streamer<?> streamer) {
        return computingStreamerSize.contains(streamer.getId());
    }

    public void stopComputingStreamerSize(Streamer<?> streamer) {
        computingStreamerSize.remove(streamer.getId());
    }
}
