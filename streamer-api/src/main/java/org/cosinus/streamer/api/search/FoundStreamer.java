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

package org.cosinus.streamer.api.search;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerDelegate;
import org.cosinus.streamer.api.BinaryStreamerDelegate;

import java.nio.file.Path;
import java.util.stream.Stream;

public class FoundStreamer<S extends Streamer<S>> extends StreamerDelegate<S, S> {

    private final SearchStreamer<S> parent;

    public FoundStreamer(final S delegate, final SearchStreamer<S> parent) {
        super(delegate);
        this.parent = parent;
    }

    @Override
    public BinaryStreamer binaryStreamer() {
        return new BinaryStreamerDelegate(super.binaryStreamer(), parent);
    }

    @Override
    public SearchStreamer<S> getParent() {
        return parent;
    }

    @Override
    public Stream<S> stream() {
        return delegate.stream();
    }

    @Override
    public Path getPath() {
        return parent.getPath().resolve(delegate.getName());
    }

    @Override
    public String getDescription() {
        return getPath().toString();
    }

    @Override
    public boolean isLink() {
        return true;
    }

    @Override
    public Streamer<?> getLinkedStreamer() {
        return delegate;
    }
}
