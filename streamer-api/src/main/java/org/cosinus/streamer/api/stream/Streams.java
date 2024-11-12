package org.cosinus.streamer.api.stream;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Streams generics
 */
public class Streams {

    public static <T> Stream<T> stream(Iterator<T> sourceIterator) {
        return stream(sourceIterator, false);
    }

    public static <T> Stream<T> stream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }
}
