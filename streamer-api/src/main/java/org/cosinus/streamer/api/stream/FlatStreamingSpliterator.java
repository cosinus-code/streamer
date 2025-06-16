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

package org.cosinus.streamer.api.stream;

import org.cosinus.streamer.api.Streamer;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.util.stream.Collectors.toList;

/**
 * Spliterator for flattening a tree of streams
 */
public class FlatStreamingSpliterator<S extends Streamer> implements Spliterator<S>
{

    private final FlatStreamingStrategy strategy;

    private final Queue<S> streamers;

    private final StreamSupplier<S> streamSupplier;

    private final Map<String, S> streamedMap;

    public FlatStreamingSpliterator(
        final FlatStreamingStrategy strategy,
        final Stream<S> streamers) {
        this(strategy, streamers, Streamer::stream);
    }

    public FlatStreamingSpliterator(
        final FlatStreamingStrategy strategy,
        final Stream<S> streamers,
        final StreamSupplier<S> streamSupplier)
    {
        this.strategy = strategy;
        this.streamers = strategy.isDepthFirst() ? new ConcurrentLinkedDeque<>() : new ConcurrentLinkedQueue<>();
        this.streamSupplier = streamSupplier;
        this.streamedMap = new HashMap<>();
        streamers.forEach(this.streamers::add);
    }

    @Override
    public boolean tryAdvance(Consumer<? super S> action) {
        S streamer = streamers.peek();
        if (streamer == null) {
            return false;
        }

        boolean isStreamableButNotYetStreamed = streamer.isParent() && isStreamed(streamer);
        if (strategy.isParentFirst() || !isStreamableButNotYetStreamed) {
            action.accept(streamers.poll());
        }

        if (isStreamableButNotYetStreamed) {
            try(Stream<S> stream = streamSupplier.apply(streamer)) {
                pushInQueue(stream);
            }
            setStreamed(streamer);
        }

        return true;
    }

    private void setStreamed(final S streamer) {
        streamedMap.put(streamer.getId(), streamer);
    }

    private boolean isStreamed(final S streamer) {
        return streamedMap.get(streamer.getId()) == null;
    }

    private void pushInQueue(Stream<? extends S> stream) {
        if (streamers instanceof Deque<S> deque) {
            reverse(stream).forEach(deque::push);
        }
        else {
            stream.forEach(streamers::add);
        }
    }

    private Stream<? extends S> reverse(Stream<? extends S> stream) {
        List<? extends S> streamersList = stream.collect(toList());
        Collections.reverse(streamersList);
        return streamersList.stream();
    }

    @Override
    public Spliterator<S> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }
}
