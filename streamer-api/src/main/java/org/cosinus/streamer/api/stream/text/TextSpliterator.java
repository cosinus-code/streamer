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
package org.cosinus.streamer.api.stream.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;

/**
 * Generic {@link java.util.Spliterator} for streaming input streams as chunks of binary data.
 * <p>
 * The size of chunks is configurable in the constructor.
 */
public class TextSpliterator extends AbstractSpliterator<String> {

    private final BufferedReader reader;

    public TextSpliterator(final BufferedReader reader) {
        super(MAX_VALUE, ORDERED | NONNULL);
        this.reader = reader;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super String> action) {
        String line = tryRead();
        if (line == null) {
            return false;
        }

        action.accept(line);
        return true;
    }

    /**
     * Try to read a new line.
     * <p>
     * If no data is available anymore, it should return null;
     *
     * @return the read line
     */
    protected String tryRead() {
        try {
            return reader.readLine();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
