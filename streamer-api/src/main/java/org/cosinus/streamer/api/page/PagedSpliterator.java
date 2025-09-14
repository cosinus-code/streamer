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

package org.cosinus.streamer.api.page;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class PagedSpliterator<T> implements Spliterator<T> {

    private static final int PAGE_SIZE = 200;

    private final PageSupplier<T> pageSupplier;

    private final Queue<T> activities;

    private int page = 1;

    public PagedSpliterator(final PageSupplier<T> pageSupplier) {
        injectContext(this);
        this.pageSupplier = pageSupplier;
        this.activities = new LinkedList<>();
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        if (activities.isEmpty()) {
            activities.addAll(getActivities());
        }

        if (activities.isEmpty()) {
            return false;
        }

        action.accept(activities.poll());
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
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

    protected List<T> getActivities() {
        return pageSupplier.call(PAGE_SIZE, page++);
    }
}
