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

package org.cosinus.streamer.zip.stream;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;

/**
 * Spliterator for zip entries
 */
public class ZipSpliterator implements Spliterator<ZipStreamEntry> {

    private final ZipEntryInputStream zipInputStream;

    public ZipSpliterator(ZipEntryInputStream zipInputStream) {
        this.zipInputStream = zipInputStream;
    }

    @Override
    public boolean tryAdvance(Consumer<? super ZipStreamEntry> action) {
        Optional<ZipStreamEntry> entry = nextEntry();
        entry.ifPresent(action);
        return entry.isPresent();
    }

    protected Optional<ZipStreamEntry> nextEntry() {
        try {
            return Optional.ofNullable(zipInputStream.getNextEntry());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Spliterator<ZipStreamEntry> trySplit() {
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
