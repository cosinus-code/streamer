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
package org.cosinus.streamer.api.stream.text;

import org.cosinus.streamer.api.stream.StreamDelegate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

public class TextStream extends StreamDelegate<String> {
    private final BufferedReader reader;

    public TextStream(Stream<String> delegate, final BufferedReader reader)
    {
        super(delegate);
        this.reader = reader;
    }

    public static Stream<String> of(final InputStream inputStream) {
        return of(new InputStreamReader(inputStream));
    }

    public static Stream<String> of(final InputStreamReader reader) {
        return of(new BufferedReader(reader));
    }

    public static Stream<String> of(final BufferedReader reader) {
        requireNonNull(reader);
        TextSpliterator spliterator = new TextSpliterator(reader);
        return new TextStream(stream(spliterator, false), reader);
    }

    public static Stream<String> lines(final InputStream inputStream) {
        return of(new InputStreamReader(inputStream));
    }

    public static Stream<String> lines(final InputStreamReader reader) {
        return of(new BufferedReader(reader));
    }

    public static Stream<String> lines(final BufferedReader reader) {
        requireNonNull(reader);
        return reader.lines();
    }

    @Override
    public void close()
    {
        super.close();

        try {
            reader.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
