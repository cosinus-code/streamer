package org.cosinus.streamer.api.stream.pipeline;

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;

import java.io.IOException;
import java.util.stream.Stream;

public interface Pipeline<D, I extends Stream<D>, O extends StreamConsumer<D>, T extends PipelineStrategy> {

    I openPipelineInputStream(T pipelineStrategy);

    O openPipelineOutputStream(T pipelineStrategy);

    T getPipelineStrategy();

    default void consume() throws IOException {
        T pipelineStrategy = getPipelineStrategy();
        try (I pipelineInputStream = openPipelineInputStream(pipelineStrategy);
             O pipelineOutputStream = openPipelineOutputStream(pipelineStrategy)) {

            if (prepare(pipelineInputStream, pipelineOutputStream, pipelineStrategy)) {
                pipelineOutputStream.consume(
                    pipelineInputStream,
                    pipelineStrategy::shouldRetryOnFailed,
                    pipelineStrategy::finalizeChunkData);
                check(pipelineInputStream, pipelineOutputStream, pipelineStrategy);
            }
        } finally {
            finalizeData(pipelineStrategy);
        }
    }

    default void finalizeData(T pipelineStrategy) {
        pipelineStrategy.finalizeData();
    }

    boolean prepare(I pipelineInputStream,
                    O pipelineOutputStream,
                    T pipelineStrategy) throws IOException;

    void check(I pipelineInputStream,
               O pipelineOutputStream,
               T pipelineStrategy);
}
