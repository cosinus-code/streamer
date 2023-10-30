package org.cosinus.streamer.api.stream.pipeline;

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.error.SkipPipelineConsumeException;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public interface Pipeline<D, I extends Stream<D>, O extends StreamConsumer<D>, S extends PipelineStrategy> {

    I openPipelineInputStream(S pipelineStrategy);

    O openPipelineOutputStream(S pipelineStrategy);

    S getPipelineStrategy();

    PipelineListener<D> getPipelineListener();

    default void openPipeline() throws IOException {
        S pipelineStrategy = getPipelineStrategy();
        PipelineListener<D> pipelineListener = ofNullable(getPipelineListener())
            .orElseGet(() -> new PipelineListener<>(){});

        try {
            preparePipelineOpen(pipelineStrategy, pipelineListener);
        } catch (SkipPipelineConsumeException ex) {
            pipelineListener.afterPipelineDataSkip(ex.getSkippedSize());
            return;
        }

        pipelineListener.beforePipelineOpen();
        try (I pipelineInputStream = openPipelineInputStream(pipelineStrategy);
             O pipelineOutputStream = openPipelineOutputStream(pipelineStrategy)) {

            pipelineListener.afterPipelineOpen();
            preparePipelineConsume(pipelineInputStream, pipelineOutputStream, pipelineStrategy, pipelineListener);
            pipelineOutputStream.consume(
                pipelineInputStream,
                ofNullable(pipelineStrategy)
                    .map(strategy -> (Supplier<Boolean>) strategy::shouldRetryOnFail)
                    .orElse(null),
                pipelineListener::beforePipelineDataConsume,
                pipelineListener::afterPipelineDataConsume);
            checkPipelineConsume(pipelineInputStream, pipelineOutputStream, pipelineStrategy, pipelineListener);
            pipelineListener.beforePipelineClose();
        } catch (SkipPipelineConsumeException ex) {
            pipelineListener.afterPipelineDataSkip(ex.getSkippedSize());
        } finally {
            pipelineListener.afterPipelineClose();
        }
    }

    default void preparePipelineOpen(S pipelineStrategy, PipelineListener<D> pipelineListener) {
    }

    default void preparePipelineConsume(I pipelineInputStream,
                                        O pipelineOutputStream,
                                        S pipelineStrategy,
                                        PipelineListener<D> listener) throws IOException {
    }

    default void checkPipelineConsume(I pipelineInputStream,
                                      O pipelineOutputStream,
                                      S pipelineStrategy,
                                      PipelineListener<D> listener) throws IOException {
    }
}
