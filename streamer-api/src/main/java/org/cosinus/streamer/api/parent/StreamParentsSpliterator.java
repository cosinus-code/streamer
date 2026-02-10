/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.api.parent;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;

public class StreamParentsSpliterator extends AbstractSpliterator<ParentStreamer<?>> {

    private final AtomicReference<ParentStreamer<?>> currentParent;

    public StreamParentsSpliterator(final Streamer<?> initialStreamer) {
        super(MAX_VALUE, ORDERED | NONNULL);
        this.currentParent = new AtomicReference<>(initialStreamer.getParent());
    }

    @Override
    public boolean tryAdvance(Consumer<? super ParentStreamer<?>> action) {
        ParentStreamer<?> parent = currentParent.get();
        if (parent == null) {
            return false;
        }

        action.accept(parent);
        currentParent.set(parent.getParent());
        return true;
    }
}
