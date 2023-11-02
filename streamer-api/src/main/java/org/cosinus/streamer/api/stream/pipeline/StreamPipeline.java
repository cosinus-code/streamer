package org.cosinus.streamer.api.stream.pipeline;

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;

import java.util.stream.Stream;

public interface StreamPipeline<T> extends Pipeline<T, Stream<T>, StreamConsumer<T>, PipelineStrategy> {

    @Override
    default PipelineStrategy getPipelineStrategy()
    {
        return new NoPipelineStrategy();
    }

    @Override
    default PipelineListener<T> getPipelineListener()
    {
        return null;
    }
}
