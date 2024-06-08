package org.cosinus.streamer.api.stream.pipeline;

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.pipeline.error.SkipPipelineConsumeException;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * Pipeline interface.
 *
 * @param <D> the type of data consumed through the pipeline
 * @param <I> the type of input stream
 * @param <O> the type of output stream consumer
 * @param <S> the type of stream pipeline strategy
 */
public interface Pipeline<D, I extends Stream<D>, O extends StreamConsumer<D>, S extends PipelineStrategy> {

    I openPipelineInputStream(S pipelineStrategy);

    O openPipelineOutputStream(S pipelineStrategy);

    S getPipelineStrategy();

    PipelineListener<D> getPipelineListener();

    default void openPipeline() throws IOException {
        S pipelineStrategy = getPipelineStrategy();
        PipelineListener<D> pipelineListener = ofNullable(getPipelineListener())
            .orElseGet(() -> new PipelineListener<>() {
            });

        try {
            preparePipelineOpen(pipelineStrategy, pipelineListener);
        } catch (SkipPipelineConsumeException ex) {
            pipelineListener.afterPipelineDataSkip(ex.getSkippedSize());
            return;
        }

        boolean pipelineFailed = false;
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
        } catch (Exception ex) {
            pipelineFailed = true;
            pipelineListener.onPipelineFail();
            throw ex;
        } finally {
            pipelineListener.afterPipelineClose(pipelineFailed);
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
