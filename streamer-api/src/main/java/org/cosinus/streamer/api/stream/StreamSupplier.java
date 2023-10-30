package org.cosinus.streamer.api.stream;

import org.cosinus.streamer.api.Streamer;

import java.util.function.Function;
import java.util.stream.Stream;

public interface StreamSupplier<S extends Streamer<?>>
    extends Function<Streamer<? extends S>, Stream<? extends S>>
{
}
