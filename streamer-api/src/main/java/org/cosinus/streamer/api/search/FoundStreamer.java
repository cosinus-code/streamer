package org.cosinus.streamer.api.search;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerDelegate;
import org.cosinus.streamer.api.stream.BinaryStreamerDelegate;

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
}
