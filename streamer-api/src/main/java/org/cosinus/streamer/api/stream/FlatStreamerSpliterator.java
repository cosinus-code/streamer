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

package org.cosinus.streamer.api.stream;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;

/**
 * Spliterator for flattening a tree of streams
 */
public class FlatStreamerSpliterator<T> implements Spliterator<T> {

    private final ArrayDeque<Iterator<T>> streams;

    private final Function<T, Stream<T>> streamSupplier;

    public FlatStreamerSpliterator(Stream<T> initial,
                                   Function<T, Stream<T>> streamSupplier) {
        this.streams = new ArrayDeque<>();
        streams.add(initial.iterator());
        this.streamSupplier = streamSupplier;
    }


    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Iterator<T> iterator = streams.peek();
        if (iterator == null) {
            return false;
        }

        if (!iterator.hasNext()) {
            streams.poll();
            return tryAdvance(action);
        }

        T item = iterator.next();
        streams.add(streamSupplier.apply(item).iterator());
        action.accept(item);
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        //TODO: can split by sibling tree nodes
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
